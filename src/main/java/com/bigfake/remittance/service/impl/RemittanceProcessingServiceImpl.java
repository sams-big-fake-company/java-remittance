package com.bigfake.remittance.service.impl;

import com.bigfake.remittance.client.CashApplicationClient;
import com.bigfake.remittance.domain.DeductionReasonCode;
import com.bigfake.remittance.domain.OpenInvoice;
import com.bigfake.remittance.domain.Payer;
import com.bigfake.remittance.domain.PaymentInstruction;
import com.bigfake.remittance.domain.PostalAddress;
import com.bigfake.remittance.domain.RemittanceAdvice;
import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.domain.enums.AdviceStatus;
import com.bigfake.remittance.domain.enums.FileStatus;
import com.bigfake.remittance.domain.enums.InstructionStatus;
import com.bigfake.remittance.domain.enums.InvoiceStatus;
import com.bigfake.remittance.domain.enums.LineStatus;
import com.bigfake.remittance.domain.enums.MatchType;
import com.bigfake.remittance.domain.enums.PayerStatus;
import com.bigfake.remittance.domain.enums.RejectReason;
import com.bigfake.remittance.dto.FileSummaryDto;
import com.bigfake.remittance.dto.ManualMatchRequest;
import com.bigfake.remittance.dto.PayerRequest;
import com.bigfake.remittance.dto.RejectDto;
import com.bigfake.remittance.exception.ConflictException;
import com.bigfake.remittance.exception.NotFoundException;
import com.bigfake.remittance.repository.DeductionReasonCodeRepository;
import com.bigfake.remittance.repository.OpenInvoiceRepository;
import com.bigfake.remittance.repository.PaymentInstructionRepository;
import com.bigfake.remittance.repository.PayerRepository;
import com.bigfake.remittance.repository.RemittanceAdviceRepository;
import com.bigfake.remittance.repository.RemittanceFileRepository;
import com.bigfake.remittance.repository.RemittanceLineRepository;
import com.bigfake.remittance.service.MatchingService;
import com.bigfake.remittance.service.DuplicateFileAuditService;
import com.bigfake.remittance.service.RemittanceProcessingService;
import com.bigfake.remittance.util.AmountUtils;
import com.bigfake.remittance.util.ChecksumUtils;
import com.bigfake.remittance.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class RemittanceProcessingServiceImpl implements RemittanceProcessingService {
    private final RemittanceFileRepository files;
    private final RemittanceLineRepository lines;
    private final PayerRepository payers;
    private final DeductionReasonCodeRepository reasons;
    private final OpenInvoiceRepository invoices;
    private final RemittanceAdviceRepository advices;
    private final PaymentInstructionRepository instructions;
    private final MatchingService matching;
    private final CashApplicationClient cashClient;
    private final DuplicateFileAuditService duplicateFileAudit;
    public RemittanceProcessingServiceImpl(
            RemittanceFileRepository files,
            RemittanceLineRepository lines,
            PayerRepository payers,
            DeductionReasonCodeRepository reasons,
            OpenInvoiceRepository invoices,
            RemittanceAdviceRepository advices,
            PaymentInstructionRepository instructions,
            MatchingService matching,
            CashApplicationClient cashClient,
            DuplicateFileAuditService duplicateFileAudit) {
        this.files = files;
        this.lines = lines;
        this.payers = payers;
        this.reasons = reasons;
        this.invoices = invoices;
        this.advices = advices;
        this.instructions = instructions;
        this.matching = matching;
        this.cashClient = cashClient;
        this.duplicateFileAudit = duplicateFileAudit;
    }
    @Override
    public FileSummaryDto ingest(String fileName, byte[] content) {
        String checksum = ChecksumUtils.sha256(content);
        Optional<RemittanceFile> duplicate = files.findByChecksum(checksum);
        if (duplicate.isPresent()) {
            RemittanceFile rejected = new RemittanceFile();
            rejected.setFileName(fileName);
            rejected.setChecksum(checksum);
            rejected.setStatus(FileStatus.REJECTED);
            rejected.setRejectReason(RejectReason.DUPLICATE_FILE);
            rejected.setOriginalFileId(duplicate.get().getId());
            duplicateFileAudit.saveRejectedDuplicate(rejected);
            throw new ConflictException("Duplicate remittance file; original id " + duplicate.get().getId());
        }
        RemittanceFile file = new RemittanceFile();
        file.setFileName(fileName);
        file.setChecksum(checksum);
        file.setStatus(FileStatus.RECEIVED);
        file.setReceivedAt(LocalDateTime.now(ZoneId.systemDefault()));
        file.setLines(new ArrayList<>());
        files.save(file);
        file.setStatus(FileStatus.PARSING);
        files.save(file);
        List<String> records = readRecords(content);
        if (records.isEmpty()) {
            return rejectFile(file, RejectReason.MALFORMED_STRUCTURE, "No records");
        }
        boolean delimited = records.get(0).contains("|");
        file.setLayout(delimited ? "DELIMITED" : "FIXED_WIDTH");
        ParsedTotals totals = new ParsedTotals();
        int detail = 0;
        int headers = 0;
        int trailers = 0;
        for (int i = 0; i < records.size(); i++) {
            String raw = records.get(i);
            if (raw.startsWith("H")) {
                headers++;
                parseHeader(file, raw, delimited);
            } else if (raw.startsWith("T")) {
                trailers++;
                parseTrailer(totals, raw, delimited);
            } else if (raw.startsWith("D")) {
                detail++;
                RemittanceLine line = parseLine(file, raw, i + 1, delimited);
                file.getLines().add(line);
                totals.amount = totals.amount.add(AmountUtils.normalize(line.getPaidAmount()));
            } else {
                RemittanceLine line = new RemittanceLine();
                line.setRemittanceFile(file);
                line.setLineNumber((long) i + 1);
                line.setRawRecord(raw);
                line.setStatus(LineStatus.REJECTED);
                line.setRejectReason(RejectReason.MALFORMED_RECORD);
                line.setRejectMessage("Unknown record type");
                file.getLines().add(line);
            }
        }
        file.setActualDetailCount((long) detail);
        file.setActualTotalAmount(AmountUtils.normalize(totals.amount));
        boolean totalsMatch = headers == 1
                && trailers == 1
                && file.getDeclaredDetailCount() != null
                && file.getDeclaredDetailCount().equals((long) detail)
                && file.getDeclaredTotalAmount() != null
                && file.getDeclaredTotalAmount().compareTo(totals.amount) == 0
                && totals.trailerCount == detail
                && totals.trailerAmount.compareTo(totals.amount) == 0;
        if (!totalsMatch) {
            return rejectFile(file, RejectReason.CONTROL_TOTAL_MISMATCH,
                    "Header/trailer totals do not equal parsed details");
        }
        int accepted = 0;
        for (RemittanceLine line : file.getLines()) {
            if (line.getStatus() != LineStatus.REJECTED) {
                accepted++;
                applyMatch(line);
            }
        }
        file.setStatus(accepted == 0 ? FileStatus.REJECTED : FileStatus.PARSED);
        files.save(file);
        return summary(file);
    }
    private List<String> readRecords(byte[] content) {
        return new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(content), StandardCharsets.UTF_8))
                .lines()
                .collect(java.util.stream.Collectors.toList());
    }

    private void parseHeader(RemittanceFile f,String raw,boolean delimited) {
        try {
            if (delimited) {
                String[] parts = raw.split("\\|", -1);
                f.setSenderBankId(parts[1]);
                f.setFileDate(DateUtils.parseLegacy(parts[2]).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
                f.setControlNumber(parts[3]);
                f.setDeclaredDetailCount(Long.valueOf(parts[4]));
                f.setDeclaredTotalAmount(AmountUtils.normalize(new BigDecimal(parts[5])));
            } else {
                f.setSenderBankId(raw.substring(1, 11).trim());
                f.setFileDate(DateUtils.parseLegacy(raw.substring(11, 19)).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
                f.setControlNumber(raw.substring(19, 33).trim());
                f.setDeclaredDetailCount(Long.valueOf(raw.substring(33, 43)));
                f.setDeclaredTotalAmount(AmountUtils.cents(Long.parseLong(raw.substring(43, 61))));
            }
        } catch (Exception exception) {
            f.setRejectReason(RejectReason.MALFORMED_STRUCTURE);
        }
    }

    private void parseTrailer(ParsedTotals totals, String raw, boolean delimited) {
        try {
            if (delimited) {
                String[] parts = raw.split("\\|", -1);
                totals.trailerCount = Integer.parseInt(parts[1]);
                totals.trailerAmount = AmountUtils.normalize(new BigDecimal(parts[2]));
            } else {
                totals.trailerCount = Integer.parseInt(raw.substring(1, 11));
                totals.trailerAmount = AmountUtils.cents(Long.parseLong(raw.substring(11, 29)));
            }
        } catch (Exception exception) {
            totals.trailerCount = -1;
        }
    }

    private RemittanceLine parseLine(RemittanceFile f,String raw,int number,boolean delimited) {
        RemittanceLine line = new RemittanceLine();
        line.setRemittanceFile(f);
        line.setLineNumber((long) number);
        line.setRawRecord(raw);
        line.setStatus(LineStatus.ACCEPTED);
        try {
            String payer;
            String invoice;
            String deduction;
            String date;
            BigDecimal paid;
            BigDecimal invoiced;
            if (delimited) {
                String[] parts = raw.split("\\|", -1);
                if (parts.length < 8) {
                    throw new IllegalArgumentException("Delimited record has too few fields");
                }
                payer = parts[1];
                invoice = parts[2];
                paid = AmountUtils.normalize(new BigDecimal(parts[3]));
                invoiced = AmountUtils.normalize(new BigDecimal(parts[4]));
                deduction = parts[5];
                date = parts[6];
            } else {
                if (raw.length() < 120) {
                    throw new IllegalArgumentException("Fixed-width record is too short");
                }
                payer = raw.substring(1, 11).trim();
                invoice = raw.substring(11, 31).trim();
                paid = AmountUtils.cents(Long.parseLong(raw.substring(31, 49)));
                invoiced = AmountUtils.cents(Long.parseLong(raw.substring(49, 67)));
                deduction = raw.substring(67, 71).trim();
                date = raw.substring(71, 79);
            }
            line.setPayerCode(payer);
            line.setInvoiceReference(invoice);
            line.setPaidAmount(paid);
            line.setInvoicedAmount(invoiced);
            line.setDeductionReasonCode(deduction);
            line.setRemittanceDate(DateUtils.parseLegacy(date).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
            validateLine(line);
        } catch (Exception exception) {
            reject(line, RejectReason.MALFORMED_RECORD, "Unable to parse remittance line");
        }
        return line;
    }
    private void validateLine(RemittanceLine l) {
        Optional<Payer> payer = payers.findByPayerCode(l.getPayerCode());
        if (!payer.isPresent()) {
            reject(l, RejectReason.UNKNOWN_PAYER, "Payer code was not found");
            return;
        }
        if (payer.get().getStatus() != PayerStatus.ACTIVE) {
            reject(l, RejectReason.INACTIVE_PAYER, "Payer is inactive");
            return;
        }
        if (l.getPaidAmount().compareTo(BigDecimal.ZERO) <= 0
                || l.getInvoicedAmount().compareTo(BigDecimal.ZERO) < 0) {
            reject(l, RejectReason.INVALID_AMOUNT, "Amounts must be positive and non-negative");
            return;
        }
        if (l.getRemittanceDate().isAfter(LocalDate.now(ZoneId.systemDefault()).plusDays(90))) {
            reject(l, RejectReason.INVALID_DATE, "Remittance date is too far in the future");
            return;
        }
        if (l.getInvoiceReference() == null || l.getInvoiceReference().trim().isEmpty()) {
            reject(l, RejectReason.MISSING_INVOICE_REFERENCE, "Invoice reference is required");
            return;
        }
        if (l.getPaidAmount().compareTo(l.getInvoicedAmount()) < 0) {
            if (l.getDeductionReasonCode() == null
                    || l.getDeductionReasonCode().trim().isEmpty()) {
                reject(l, RejectReason.MISSING_DEDUCTION_CODE,
                        "Deduction reason required for short pay");
                return;
            }
            Optional<DeductionReasonCode> code = reasons.findById(l.getDeductionReasonCode());
            if (!code.isPresent() || !Boolean.TRUE.equals(code.get().getActive())) {
                reject(l, RejectReason.INVALID_DEDUCTION_CODE,
                        "Deduction reason is not active");
            }
        }
    }

    private void applyMatch(RemittanceLine line) {
        MatchingService.MatchResult result = matching.match(line);
        line.setMatchType(result.type);
        if (result.invoice == null) {
            line.setStatus(LineStatus.UNMATCHED);
            return;
        }
        OpenInvoice invoice = result.invoice;
        line.setOpenInvoiceId(invoice.getId());
        applyAmount(line, invoice);
    }

    private void applyAmount(RemittanceLine line, OpenInvoice invoice) {
        int comparison = line.getPaidAmount().compareTo(invoice.getOutstandingAmount());
        if (comparison == 0) {
            invoice.setOutstandingAmount(BigDecimal.ZERO);
            invoice.setStatus(InvoiceStatus.PAID);
            line.setStatus(LineStatus.MATCHED);
        } else if (comparison < 0) {
            invoice.setOutstandingAmount(invoice.getOutstandingAmount()
                    .subtract(line.getPaidAmount()).setScale(2));
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            line.setStatus(LineStatus.SHORT_PAID);
        } else {
            line.setUnappliedAmount(line.getPaidAmount()
                    .subtract(invoice.getOutstandingAmount()));
            invoice.setOutstandingAmount(BigDecimal.ZERO);
            invoice.setStatus(InvoiceStatus.PAID);
            line.setStatus(LineStatus.OVERPAID);
        }
        invoices.save(invoice);
    }

    private FileSummaryDto rejectFile(RemittanceFile file, RejectReason reason, String message) {
        file.setStatus(FileStatus.REJECTED);
        file.setRejectReason(reason);
        file.setRejectMessage(message);
        if (file.getActualDetailCount() == null) {
            file.setActualDetailCount(0L);
        }
        files.save(file);
        return summary(file);
    }

    private void reject(RemittanceLine line, RejectReason reason, String message) {
        line.setStatus(LineStatus.REJECTED);
        line.setRejectReason(reason);
        line.setRejectMessage(message);
    }

    private FileSummaryDto summary(RemittanceFile file) {
        return new FileSummaryDto(
                file.getId(),
                file.getFileName(),
                file.getStatus().name(),
                file.getRejectReason() == null ? null : file.getRejectReason().name(),
                file.getActualDetailCount() == null ? 0 : file.getActualDetailCount(),
                file.getActualTotalAmount());
    }

    @Override
    public Page<RemittanceFile> listFiles(String status, Pageable pageable) {
        return status == null
                ? files.findAll(pageable)
                : files.findByStatus(FileStatus.valueOf(status), pageable);
    }

    @Override
    public RemittanceFile getFile(Long id) {
        return files.findWithLines(id)
                .orElseThrow(() -> new NotFoundException("Remittance file not found: " + id));
    }

    @Override
    public List<RejectDto> rejects(Long id) {
        return getFile(id).getLines().stream()
                .filter(line -> line.getStatus() == LineStatus.REJECTED)
                .map(line -> new RejectDto(
                        line.getLineNumber(),
                        line.getRawRecord(),
                        line.getRejectReason().name(),
                        line.getRejectMessage()))
                .collect(java.util.stream.Collectors.toList());
    }
    @Override
    public RemittanceFile post(Long id) {
        RemittanceFile file = getFile(id);
        if (file.getStatus() != FileStatus.PARSED) {
            throw new ConflictException("Only PARSED files can be posted");
        }
        Map<String, List<RemittanceLine>> groups = file.getLines().stream()
                .filter(line -> line.getStatus() != LineStatus.REJECTED)
                .collect(java.util.stream.Collectors.groupingBy(
                        line -> line.getPayerCode() + "|" + line.getRemittanceDate()));
        for (Map.Entry<String, List<RemittanceLine>> entry : groups.entrySet()) {
            String[] key = entry.getKey().split("\\|");
            LocalDate adviceDate = LocalDate.parse(key[1]);
            long sequence = advices.countByRemittanceDate(adviceDate) + 1;
        // TODO REM-5710: protect advice sequence allocation against concurrent posts. // NOSONAR
            RemittanceAdvice advice = new RemittanceAdvice();
            advice.setAdviceNumber("ADV-" + key[1].replace("-", "")
                    + "-" + String.format("%04d", sequence));
            advice.setRemittanceFileId(id);
            advice.setPayerCode(key[0]);
            advice.setRemittanceDate(adviceDate);
            advice.setLineCount((long) entry.getValue().size());
            advice.setTotalPaidAmount(entry.getValue().stream()
                    .map(RemittanceLine::getPaidAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            advice.setStatus(AdviceStatus.POSTED);
            advice = advices.save(advice);

            PaymentInstruction instruction = new PaymentInstruction();
            instruction.setInstructionReference("PI-" + key[1].replace("-", "")
                    + "-" + advice.getId());
            instruction.setRemittanceAdviceId(advice.getId());
            instruction.setPayerCode(advice.getPayerCode());
            instruction.setAmount(advice.getTotalPaidAmount());
            instruction.setStatus(InstructionStatus.PENDING);
            instructions.save(instruction);
        }
        file.setStatus(FileStatus.POSTED);
        return files.save(file);
    }

    @Override
    public Page<RemittanceAdvice> listAdvices(String payer, String status, Pageable pageable) {
        if (payer != null && status != null) {
            return advices.findByPayerCodeAndStatus(payer, AdviceStatus.valueOf(status), pageable);
        }
        if (payer != null) {
            return advices.findByPayerCode(payer, pageable);
        }
        return advices.findAll(pageable);
    }

    @Override
    public RemittanceAdvice advice(String number) {
        return advices.findByAdviceNumber(number)
                .orElseThrow(() -> new NotFoundException("Advice not found: " + number));
    }

    @Override
    public Page<RemittanceLine> exceptions(String payer, Pageable pageable) {
        if (payer == null) {
            return lines.findByStatus(LineStatus.UNMATCHED, pageable);
        }
        return lines.findByStatusAndPayerCode(LineStatus.UNMATCHED, payer, pageable);
    }

    @Override
    public RemittanceLine manualMatch(Long id, ManualMatchRequest request) {
        RemittanceLine line = lines.findById(id)
                .orElseThrow(() -> new NotFoundException("Line not found: " + id));
        String normalized = request.getInvoiceReference()
                .toUpperCase().replaceAll("[^A-Z0-9]", "");
        OpenInvoice invoice = invoices.findOpenByNormalizedReference(normalized)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
        if (line.getPaidAmount().compareTo(invoice.getOutstandingAmount()) < 0) {
            Optional<DeductionReasonCode> reason = request.getDeductionReasonCode() == null
                    ? Optional.empty()
                    : reasons.findById(request.getDeductionReasonCode());
            if (!reason.isPresent() || !Boolean.TRUE.equals(reason.get().getActive())) {
                throw new ConflictException("Active deduction reason is required");
            }
            line.setDeductionReasonCode(request.getDeductionReasonCode());
        }
        line.setMatchType(MatchType.MANUAL);
        line.setOpenInvoiceId(invoice.getId());
        applyAmount(line, invoice);
        return lines.save(line);
    }

    @Override
    public Page<Payer> payers(Pageable pageable) {
        return payers.findAll(pageable);
    }

    @Override
    public Payer payer(String code) {
        return payers.findByPayerCode(code)
                .orElseThrow(() -> new NotFoundException("Payer not found: " + code));
    }

    @Override
    public Payer createPayer(PayerRequest request) {
        if (payers.findByPayerCode(request.getPayerCode()).isPresent()) {
            throw new ConflictException("Payer code already exists: " + request.getPayerCode());
        }
        Payer payer = new Payer();
        payer.setPayerCode(request.getPayerCode());
        payer.setName(request.getName());
        payer.setEmail(request.getEmail());
        payer.setStatus(PayerStatus.ACTIVE);
        PostalAddress address = new PostalAddress();
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        payer.setAddress(address);
        return payers.save(payer);
    }

    @Override
    public void dispatchPending() {
        for (PaymentInstruction instruction : instructions.findByStatus(InstructionStatus.PENDING)) {
            if (cashClient.send(instruction)) {
                instruction.setStatus(InstructionStatus.ACKNOWLEDGED);
                instruction.setSentAt(LocalDateTime.now(ZoneId.systemDefault()));
            } else {
                instruction.setStatus(InstructionStatus.FAILED);
                instruction.setFailureReason("Downstream rejected instruction");
            }
            instructions.save(instruction);
        }
    }

    private static class ParsedTotals {
        private long trailerCount = -1;
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal trailerAmount = BigDecimal.ZERO;
    }
}
