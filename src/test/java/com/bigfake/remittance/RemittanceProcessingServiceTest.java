package com.bigfake.remittance;

import com.bigfake.remittance.client.CashApplicationClient;
import com.bigfake.remittance.domain.DeductionReasonCode;
import com.bigfake.remittance.domain.OpenInvoice;
import com.bigfake.remittance.domain.Payer;
import com.bigfake.remittance.domain.PaymentInstruction;
import com.bigfake.remittance.domain.RemittanceAdvice;
import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.domain.enums.FileStatus;
import com.bigfake.remittance.domain.enums.AdviceStatus;
import com.bigfake.remittance.domain.enums.InstructionStatus;
import com.bigfake.remittance.domain.enums.InvoiceStatus;
import com.bigfake.remittance.domain.enums.LineStatus;
import com.bigfake.remittance.domain.enums.MatchType;
import com.bigfake.remittance.domain.enums.PayerStatus;
import com.bigfake.remittance.exception.ConflictException;
import com.bigfake.remittance.repository.DeductionReasonCodeRepository;
import com.bigfake.remittance.repository.OpenInvoiceRepository;
import com.bigfake.remittance.repository.PaymentInstructionRepository;
import com.bigfake.remittance.repository.PayerRepository;
import com.bigfake.remittance.repository.RemittanceAdviceRepository;
import com.bigfake.remittance.repository.RemittanceFileRepository;
import com.bigfake.remittance.repository.RemittanceLineRepository;
import com.bigfake.remittance.service.MatchingService;
import com.bigfake.remittance.service.DuplicateFileAuditService;
import com.bigfake.remittance.service.impl.RemittanceProcessingServiceImpl;
import com.bigfake.remittance.dto.ManualMatchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemittanceProcessingServiceTest {

    @Mock
    private RemittanceFileRepository files;
    @Mock
    private RemittanceLineRepository lines;
    @Mock
    private PayerRepository payers;
    @Mock
    private DeductionReasonCodeRepository reasons;
    @Mock
    private OpenInvoiceRepository invoices;
    @Mock
    private RemittanceAdviceRepository advices;
    @Mock
    private PaymentInstructionRepository instructions;
    @Mock
    private MatchingService matching;
    @Mock
    private CashApplicationClient cashClient;
    @Mock
    private DuplicateFileAuditService duplicateFileAudit;

    private RemittanceProcessingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RemittanceProcessingServiceImpl(files, lines, payers, reasons, invoices,
                advices, instructions, matching, cashClient, duplicateFileAudit);
        lenient().when(files.findByChecksum(any())).thenReturn(Optional.empty());
        lenient().when(files.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void controlTotalsRejectWholeFile() {
        RemittanceFile result = ingest("H|BANK|20260810|CTRL|2|10.00\nT|1|5.00");
        assertEquals("CONTROL_TOTAL_MISMATCH", result.getRejectReason().name());
        assertEquals(FileStatus.REJECTED, result.getStatus());
    }

    @Test
    void unknownPayerRejectsLine() {
        when(payers.findByPayerCode("UNKNOWN")).thenReturn(Optional.empty());
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|5.00\n"
                + "D|UNKNOWN|INV-1|5.00|5.00||20260810|memo\nT|1|5.00");
        assertEquals(LineStatus.REJECTED, file.getLines().get(0).getStatus());
        assertEquals("UNKNOWN_PAYER", file.getLines().get(0).getRejectReason().name());
    }

    @Test
    void inactivePayerRejectsLine() {
        Payer payer = payer(PayerStatus.INACTIVE);
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|5.00\n"
                + "D|PAYER|INV-1|5.00|5.00||20260810|memo\nT|1|5.00");
        assertEquals("INACTIVE_PAYER", file.getLines().get(0).getRejectReason().name());
    }

    @Test
    void nonPositiveAmountRejectsLine() {
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|0.00\n"
                + "D|PAYER|INV-1|0.00|0.00||20260810|memo\nT|1|0.00");
        assertEquals("INVALID_AMOUNT", file.getLines().get(0).getRejectReason().name());
    }

    @Test
    void futureDateRejectsLine() {
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        LocalDate date = LocalDate.now().plusDays(91);
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|5.00\n"
                + "D|PAYER|INV-1|5.00|5.00||" + date.toString().replace("-", "") + "|memo\nT|1|5.00");
        assertEquals("INVALID_DATE", file.getLines().get(0).getRejectReason().name());
    }

    @Test
    void blankInvoiceReferenceRejectsLine() {
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|5.00\n"
                + "D|PAYER||5.00|5.00||20260810|memo\nT|1|5.00");
        assertEquals("MISSING_INVOICE_REFERENCE", file.getLines().get(0).getRejectReason().name());
    }

    @Test
    void missingDeductionRejectsShortPay() {
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|5.00\n"
                + "D|PAYER|INV-1|5.00|10.00||20260810|memo\nT|1|5.00");
        assertEquals("MISSING_DEDUCTION_CODE", file.getLines().get(0).getRejectReason().name());
    }

    @Test
    void inactiveDeductionRejectsShortPay() {
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        DeductionReasonCode reason = new DeductionReasonCode();
        reason.setActive(false);
        when(reasons.findById("SHRT")).thenReturn(Optional.of(reason));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|5.00\n"
                + "D|PAYER|INV-1|5.00|10.00|SHRT|20260810|memo\nT|1|5.00");
        assertEquals("INVALID_DEDUCTION_CODE", file.getLines().get(0).getRejectReason().name());
    }

    @Test
    void exactApplicationMarksInvoicePaid() {
        OpenInvoice invoice = invoice("INV1", "10.00");
        acceptedLine("INV1", "10.00");
        when(matching.match(any())).thenReturn(new MatchingService.MatchResult(invoice, MatchType.EXACT_REFERENCE));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|10.00\n"
                + "D|PAYER|INV1|10.00|10.00||20260810|memo\nT|1|10.00");
        assertEquals(LineStatus.MATCHED, file.getLines().get(0).getStatus());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
    }

    @Test
    void shortApplicationLeavesOutstandingBalance() {
        OpenInvoice invoice = invoice("INV1", "10.00");
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        DeductionReasonCode reason = new DeductionReasonCode();
        reason.setActive(true);
        when(reasons.findById("SHRT")).thenReturn(Optional.of(reason));
        when(matching.match(any())).thenReturn(new MatchingService.MatchResult(invoice, MatchType.EXACT_REFERENCE));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|5.00\n"
                + "D|PAYER|INV1|5.00|10.00|SHRT|20260810|memo\nT|1|5.00");
        assertEquals(LineStatus.SHORT_PAID, file.getLines().get(0).getStatus());
        assertEquals(new BigDecimal("5.00"), invoice.getOutstandingAmount());
    }

    @Test
    void overApplicationStoresUnappliedAmount() {
        OpenInvoice invoice = invoice("INV1", "5.00");
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        when(matching.match(any())).thenReturn(new MatchingService.MatchResult(invoice, MatchType.EXACT_REFERENCE));
        RemittanceFile file = ingest("H|BANK|20260810|CTRL|1|10.00\n"
                + "D|PAYER|INV1|10.00|10.00||20260810|memo\nT|1|10.00");
        assertEquals(LineStatus.OVERPAID, file.getLines().get(0).getStatus());
        assertEquals(new BigDecimal("5.00"), file.getLines().get(0).getUnappliedAmount());
    }

    @Test
    void duplicateChecksumPersistsRejectedFile() {
        RemittanceFile original = new RemittanceFile();
        original.setId(99L);
        when(files.findByChecksum(any())).thenReturn(Optional.of(original));
        assertThrows(ConflictException.class, () -> service.ingest("duplicate.txt", "x".getBytes()));
        verify(duplicateFileAudit).saveRejectedDuplicate(any(RemittanceFile.class));
    }

    @Test
    void manualMatchAppliesShortPayLogic() {
        RemittanceLine line = new RemittanceLine();
        line.setPaidAmount(new BigDecimal("5.00"));
        OpenInvoice invoice = invoice("INV1", "10.00");
        DeductionReasonCode reason = new DeductionReasonCode();
        reason.setActive(true);
        when(lines.findById(1L)).thenReturn(Optional.of(line));
        when(invoices.findOpenByNormalizedReference("INV1")).thenReturn(Optional.of(invoice));
        when(reasons.findById("SHRT")).thenReturn(Optional.of(reason));
        when(lines.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ManualMatchRequest request = new ManualMatchRequest();
        request.setInvoiceReference("INV1");
        request.setDeductionReasonCode("SHRT");
        RemittanceLine result = service.manualMatch(1L, request);
        assertEquals(LineStatus.SHORT_PAID, result.getStatus());
    }

    @Test
    void postingNonParsedFileConflicts() {
        RemittanceFile file = new RemittanceFile();
        file.setStatus(FileStatus.RECEIVED);
        doReturn(Optional.of(file)).when(files).findWithLines(1L);
        assertThrows(ConflictException.class, () -> service.post(1L));
    }

    @Test
    void postingCreatesAdviceAndPendingInstruction() {
        RemittanceFile file = new RemittanceFile();
        file.setId(1L);
        file.setStatus(FileStatus.PARSED);
        RemittanceLine line = new RemittanceLine();
        line.setPayerCode("PAYER");
        line.setRemittanceDate(LocalDate.of(2026, 8, 10));
        line.setPaidAmount(new BigDecimal("10.00"));
        line.setStatus(LineStatus.MATCHED);
        file.setLines(Collections.singletonList(line));
        when(files.findWithLines(1L)).thenReturn(Optional.of(file));
        when(advices.countByRemittanceDate(LocalDate.of(2026, 8, 10))).thenReturn(0L);
        when(advices.save(any())).thenAnswer(invocation -> {
            RemittanceAdvice advice = invocation.getArgument(0);
            advice.setId(12L);
            return advice;
        });
        when(files.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RemittanceFile result = service.post(1L);

        assertEquals(FileStatus.POSTED, result.getStatus());
        verify(advices).save(any(RemittanceAdvice.class));
        verify(instructions).save(org.mockito.ArgumentMatchers.argThat(instruction ->
                instruction.getStatus() == InstructionStatus.PENDING
                        && new BigDecimal("10.00").equals(instruction.getAmount())));
    }

    @Test
    void dispatcherMarksInstructionFailedWhenDownstreamErrors() {
        PaymentInstruction instruction = new PaymentInstruction();
        instruction.setStatus(InstructionStatus.PENDING);
        when(instructions.findByStatus(InstructionStatus.PENDING))
                .thenReturn(Collections.singletonList(instruction));
        when(cashClient.send(instruction)).thenReturn(false);

        service.dispatchPending();

        assertEquals(InstructionStatus.FAILED, instruction.getStatus());
        assertEquals("Downstream rejected instruction", instruction.getFailureReason());
        verify(instructions).save(instruction);
    }

    @Test
    void postingSecondFileUsesNextAdviceSequenceForSameDate() {
        RemittanceFile first = parsedFile(1L);
        RemittanceFile second = parsedFile(2L);
        when(files.findWithLines(1L)).thenReturn(Optional.of(first));
        when(files.findWithLines(2L)).thenReturn(Optional.of(second));
        when(advices.countByRemittanceDate(LocalDate.of(2026, 8, 10)))
                .thenReturn(0L, 1L);
        when(advices.save(any())).thenAnswer(invocation -> {
            RemittanceAdvice advice = invocation.getArgument(0);
            advice.setId(advice.getAdviceNumber().endsWith("0001") ? 1L : 2L);
            return advice;
        });
        when(files.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.post(1L);
        service.post(2L);

        ArgumentCaptor<RemittanceAdvice> captor = ArgumentCaptor.forClass(RemittanceAdvice.class);
        verify(advices, org.mockito.Mockito.times(2)).save(captor.capture());
        assertEquals("ADV-20260810-0001", captor.getAllValues().get(0).getAdviceNumber());
        assertEquals("ADV-20260810-0002", captor.getAllValues().get(1).getAdviceNumber());
    }

    private RemittanceFile ingest(String records) {
        service.ingest("file.txt", records.getBytes());
        return captureFile();
    }

    private RemittanceFile captureFile() {
        ArgumentCaptor<RemittanceFile> captor = ArgumentCaptor.forClass(RemittanceFile.class);
        verify(files, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getAllValues().get(captor.getAllValues().size() - 1);
    }

    private RemittanceFile parsedFile(Long id) {
        RemittanceFile file = new RemittanceFile();
        file.setId(id);
        file.setStatus(FileStatus.PARSED);
        RemittanceLine line = new RemittanceLine();
        line.setPayerCode("PAYER");
        line.setRemittanceDate(LocalDate.of(2026, 8, 10));
        line.setPaidAmount(new BigDecimal("10.00"));
        line.setStatus(LineStatus.MATCHED);
        file.setLines(Collections.singletonList(line));
        return file;
    }

    private Payer payer(PayerStatus status) {
        Payer payer = new Payer();
        payer.setPayerCode("PAYER");
        payer.setStatus(status);
        return payer;
    }

    private OpenInvoice invoice(String reference, String outstanding) {
        OpenInvoice invoice = new OpenInvoice();
        invoice.setId(1L);
        invoice.setNormalizedReference(reference);
        invoice.setOutstandingAmount(new BigDecimal(outstanding));
        invoice.setStatus(InvoiceStatus.OPEN);
        return invoice;
    }

    private void acceptedLine(String reference, String amount) {
        when(payers.findByPayerCode("PAYER")).thenReturn(Optional.of(payer(PayerStatus.ACTIVE)));
        RemittanceLine line = new RemittanceLine();
        line.setInvoiceReference(reference);
        line.setPaidAmount(new BigDecimal(amount));
        line.setInvoicedAmount(new BigDecimal(amount));
    }
}
