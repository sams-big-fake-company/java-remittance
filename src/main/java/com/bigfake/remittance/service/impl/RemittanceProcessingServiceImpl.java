package com.bigfake.remittance.service.impl;

import com.bigfake.remittance.client.CashApplicationClient;
import com.bigfake.remittance.domain.*;
import com.bigfake.remittance.domain.enums.*;
import com.bigfake.remittance.domain.enums.StatusEnums.*;
import com.bigfake.remittance.dto.*;
import com.bigfake.remittance.exception.*;
import com.bigfake.remittance.repository.*;
import com.bigfake.remittance.service.*;
import com.bigfake.remittance.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*; import java.math.*; import java.nio.charset.StandardCharsets; import java.time.*; import java.util.*;

@Service @Transactional @Slf4j
public class RemittanceProcessingServiceImpl implements RemittanceProcessingService {
    private final RemittanceFileRepository files; private final RemittanceLineRepository lines;
    private final PayerRepository payers; private final DeductionReasonCodeRepository reasons;
    private final OpenInvoiceRepository invoices; private final RemittanceAdviceRepository advices;
    private final PaymentInstructionRepository instructions; private final MatchingService matching;
    private final CashApplicationClient cashClient;
    public RemittanceProcessingServiceImpl(RemittanceFileRepository files, RemittanceLineRepository lines, PayerRepository payers,
        DeductionReasonCodeRepository reasons, OpenInvoiceRepository invoices, RemittanceAdviceRepository advices,
        PaymentInstructionRepository instructions, MatchingService matching, CashApplicationClient cashClient) {
        this.files=files;this.lines=lines;this.payers=payers;this.reasons=reasons;this.invoices=invoices;this.advices=advices;
        this.instructions=instructions;this.matching=matching;this.cashClient=cashClient;
    }
    @Override public FileSummaryDto ingest(String fileName, byte[] content) {
        String checksum=ChecksumUtils.sha256(content); Optional<RemittanceFile> duplicate=files.findByChecksum(checksum);
        if(duplicate.isPresent()) { RemittanceFile rejected=new RemittanceFile(); rejected.setFileName(fileName); rejected.setChecksum(checksum);
            rejected.setStatus(FileStatus.REJECTED); rejected.setRejectReason(RejectReason.DUPLICATE_FILE); rejected.setOriginalFileId(duplicate.get().getId());
            files.save(rejected); throw new ConflictException("Duplicate remittance file; original id "+duplicate.get().getId()); }
        RemittanceFile file=new RemittanceFile(); file.setFileName(fileName);file.setChecksum(checksum);file.setStatus(FileStatus.RECEIVED);
        file.setReceivedAt(LocalDateTime.now()); file.setLines(new ArrayList<>()); files.save(file);
        List<String> records=readRecords(content); if(records.isEmpty()) return rejectFile(file,RejectReason.MALFORMED_STRUCTURE,"No records");
        boolean delimited=records.get(0).contains("|"); file.setLayout(delimited?"DELIMITED":"FIXED_WIDTH");
        ParsedTotals totals=new ParsedTotals(); int detail=0; int headers=0; int trailers=0;
        for(int i=0;i<records.size();i++) {
            String raw=records.get(i); if(raw.startsWith("H")) { headers++; parseHeader(file,raw,delimited); }
            else if(raw.startsWith("T")) { trailers++; parseTrailer(totals,raw,delimited); }
            else if(raw.startsWith("D")) { detail++; RemittanceLine line=parseLine(file,raw,i+1,delimited); file.getLines().add(line); totals.amount=totals.amount.add(AmountUtils.normalize(line.getPaidAmount())); }
            else { RemittanceLine line=new RemittanceLine(); line.setRemittanceFile(file);line.setLineNumber((long)i+1);line.setRawRecord(raw);
                line.setStatus(LineStatus.REJECTED);line.setRejectReason(RejectReason.MALFORMED_RECORD);line.setRejectMessage("Unknown record type");file.getLines().add(line); }
        }
        file.setActualDetailCount((long)detail); file.setActualTotalAmount(AmountUtils.normalize(totals.amount));
        if(headers!=1 || trailers!=1 || file.getDeclaredDetailCount()==null || !file.getDeclaredDetailCount().equals((long)detail)
            || file.getDeclaredTotalAmount()==null || file.getDeclaredTotalAmount().compareTo(totals.amount)!=0
            || totals.trailerCount!=detail || totals.trailerAmount.compareTo(totals.amount)!=0)
            return rejectFile(file,RejectReason.CONTROL_TOTAL_MISMATCH,"Header/trailer totals do not equal parsed details");
        int accepted=0; for(RemittanceLine line:file.getLines()) if(line.getStatus()!=LineStatus.REJECTED) { accepted++; applyMatch(line); }
        file.setStatus(accepted==0?FileStatus.REJECTED:FileStatus.PARSED); files.save(file);
        return summary(file);
    }
    private List<String> readRecords(byte[] content) { return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content),StandardCharsets.UTF_8)).lines().collect(java.util.stream.Collectors.toList()); }
    private void parseHeader(RemittanceFile f,String raw,boolean delimited) {
        try { if(delimited) { String[] p=raw.split("\\|",-1);f.setSenderBankId(p[1]);f.setFileDate(DateUtils.parseLegacy(p[2]).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());f.setControlNumber(p[3]);f.setDeclaredDetailCount(Long.valueOf(p[4]));f.setDeclaredTotalAmount(AmountUtils.normalize(new BigDecimal(p[5]))); }
            else { f.setSenderBankId(raw.substring(1,11).trim());f.setFileDate(DateUtils.parseLegacy(raw.substring(11,19)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());f.setControlNumber(raw.substring(19,33).trim());f.setDeclaredDetailCount(Long.valueOf(raw.substring(33,43)));f.setDeclaredTotalAmount(AmountUtils.cents(Long.parseLong(raw.substring(43,61)))); } }
        catch(Exception e) { f.setRejectReason(RejectReason.MALFORMED_STRUCTURE); }
    }
    private void parseTrailer(ParsedTotals t,String raw,boolean delimited) { try { if(delimited){String[] p=raw.split("\\|",-1);t.trailerCount=Integer.parseInt(p[1]);t.trailerAmount=AmountUtils.normalize(new BigDecimal(p[2]));}else{t.trailerCount=Integer.parseInt(raw.substring(1,11));t.trailerAmount=AmountUtils.cents(Long.parseLong(raw.substring(11,29)));}}catch(Exception e){t.trailerCount=-1;} }
    private RemittanceLine parseLine(RemittanceFile f,String raw,int number,boolean delimited) {
        RemittanceLine l=new RemittanceLine();l.setRemittanceFile(f);l.setLineNumber((long)number);l.setRawRecord(raw);l.setStatus(LineStatus.ACCEPTED);
        try { String payer,invoice,deduction,date; BigDecimal paid,invoiced;
            if(delimited){String[] p=raw.split("\\|",-1); if(p.length<8) throw new IllegalArgumentException();payer=p[1];invoice=p[2];paid=AmountUtils.normalize(new BigDecimal(p[3]));invoiced=AmountUtils.normalize(new BigDecimal(p[4]));deduction=p[5];date=p[6];}
            else { if(raw.length()<120) throw new IllegalArgumentException();payer=raw.substring(1,11).trim();invoice=raw.substring(11,31).trim();paid=AmountUtils.cents(Long.parseLong(raw.substring(31,49)));invoiced=AmountUtils.cents(Long.parseLong(raw.substring(49,67)));deduction=raw.substring(67,71).trim();date=raw.substring(71,79); }
            l.setPayerCode(payer);l.setInvoiceReference(invoice);l.setPaidAmount(paid);l.setInvoicedAmount(invoiced);l.setDeductionReasonCode(deduction);
            l.setRemittanceDate(DateUtils.parseLegacy(date).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()); validateLine(l);
        } catch(Exception e) { reject(l,RejectReason.MALFORMED_RECORD,"Unable to parse remittance line"); }
        return l;
    }
    private void validateLine(RemittanceLine l) {
        Optional<Payer> payer=payers.findByPayerCode(l.getPayerCode()); if(!payer.isPresent()){reject(l,RejectReason.UNKNOWN_PAYER,"Payer code was not found");return;}
        if(payer.get().getStatus()!=PayerStatus.ACTIVE){reject(l,RejectReason.INACTIVE_PAYER,"Payer is inactive");return;}
        if(l.getPaidAmount().compareTo(BigDecimal.ZERO)<=0 || l.getInvoicedAmount().compareTo(BigDecimal.ZERO)<0){reject(l,RejectReason.INVALID_AMOUNT,"Amounts must be positive and non-negative");return;}
        if(l.getRemittanceDate().isAfter(LocalDate.now().plusDays(90))){reject(l,RejectReason.INVALID_DATE,"Remittance date is too far in the future");return;}
        if(l.getInvoiceReference()==null || l.getInvoiceReference().trim().isEmpty()){reject(l,RejectReason.MISSING_INVOICE_REFERENCE,"Invoice reference is required");return;}
        if(l.getPaidAmount().compareTo(l.getInvoicedAmount())<0) { if(l.getDeductionReasonCode()==null||l.getDeductionReasonCode().trim().isEmpty()){reject(l,RejectReason.MISSING_DEDUCTION_CODE,"Deduction reason required for short pay");return;}
            Optional<DeductionReasonCode> code=reasons.findById(l.getDeductionReasonCode());if(!code.isPresent()||!Boolean.TRUE.equals(code.get().getActive())) reject(l,RejectReason.INVALID_DEDUCTION_CODE,"Deduction reason is not active"); }
    }
    private void applyMatch(RemittanceLine l) { MatchingService.MatchResult result=matching.match(l);l.setMatchType(result.type);
        if(result.invoice==null){l.setStatus(LineStatus.UNMATCHED);return;} OpenInvoice i=result.invoice;l.setOpenInvoiceId(i.getId());
        int comparison=l.getPaidAmount().compareTo(i.getOutstandingAmount()); if(comparison==0){i.setOutstandingAmount(BigDecimal.ZERO);i.setStatus(InvoiceStatus.PAID);l.setStatus(LineStatus.MATCHED);}
        else if(comparison<0){i.setOutstandingAmount(i.getOutstandingAmount().subtract(l.getPaidAmount()).setScale(2));i.setStatus(InvoiceStatus.PARTIALLY_PAID);l.setStatus(LineStatus.SHORT_PAID);if(l.getDeductionReasonCode()==null||l.getDeductionReasonCode().trim().isEmpty()){l.setDeductionReasonCode("UNDR");log.warn("Unexplained underpayment on line "+l.getId());}}
        else {l.setUnappliedAmount(l.getPaidAmount().subtract(i.getOutstandingAmount()));i.setOutstandingAmount(BigDecimal.ZERO);i.setStatus(InvoiceStatus.PAID);l.setStatus(LineStatus.OVERPAID);} invoices.save(i); }
    private FileSummaryDto rejectFile(RemittanceFile f,RejectReason r,String message){f.setStatus(FileStatus.REJECTED);f.setRejectReason(r);f.setActualDetailCount(f.getActualDetailCount()==null?0:f.getActualDetailCount());files.save(f);return summary(f);}
    private void reject(RemittanceLine l,RejectReason r,String m){l.setStatus(LineStatus.REJECTED);l.setRejectReason(r);l.setRejectMessage(m);}
    private FileSummaryDto summary(RemittanceFile f){return new FileSummaryDto(f.getId(),f.getFileName(),f.getStatus().name(),f.getRejectReason()==null?null:f.getRejectReason().name(),f.getActualDetailCount()==null?0:f.getActualDetailCount(),f.getActualTotalAmount());}
    public Page<RemittanceFile> listFiles(String status,Pageable p){return status==null?files.findAll(p):files.findByStatus(FileStatus.valueOf(status),p);}
    public RemittanceFile getFile(Long id){return files.findWithLines(id).orElseThrow(()->new NotFoundException("Remittance file not found: "+id));}
    public List<RejectDto> rejects(Long id){return getFile(id).getLines().stream().filter(l->l.getStatus()==LineStatus.REJECTED).map(l->new RejectDto(l.getLineNumber(),l.getRawRecord(),l.getRejectReason().name(),l.getRejectMessage())).collect(java.util.stream.Collectors.toList());}
    public RemittanceFile post(Long id){RemittanceFile f=getFile(id);if(f.getStatus()!=FileStatus.PARSED)throw new ConflictException("Only PARSED files can be posted");
        Map<String,List<RemittanceLine>> groups=f.getLines().stream().filter(l->l.getStatus()!=LineStatus.REJECTED).collect(java.util.stream.Collectors.groupingBy(l->l.getPayerCode()+"|"+l.getRemittanceDate()));
        int seq=1;for(Map.Entry<String,List<RemittanceLine>> e:groups.entrySet()){String[] key=e.getKey().split("\\|");RemittanceAdvice a=new RemittanceAdvice();a.setAdviceNumber("ADV-"+key[1].replace("-","")+"-"+String.format("%04d",seq++));a.setRemittanceFileId(id);a.setPayerCode(key[0]);a.setRemittanceDate(LocalDate.parse(key[1]));a.setLineCount((long)e.getValue().size());a.setTotalPaidAmount(e.getValue().stream().map(RemittanceLine::getPaidAmount).reduce(BigDecimal.ZERO,BigDecimal::add));a.setStatus(AdviceStatus.POSTED);a=advices.save(a);PaymentInstruction pi=new PaymentInstruction();pi.setInstructionReference("PI-"+key[1].replace("-","")+"-"+a.getId());pi.setRemittanceAdviceId(a.getId());pi.setPayerCode(a.getPayerCode());pi.setAmount(a.getTotalPaidAmount());pi.setStatus(InstructionStatus.PENDING);instructions.save(pi);}f.setStatus(FileStatus.POSTED);return files.save(f);}
    public Page<RemittanceAdvice> listAdvices(String payer,String status,Pageable p){if(payer!=null&&status!=null)return advices.findByPayerCodeAndStatus(payer,AdviceStatus.valueOf(status),p);return payer!=null?advices.findByPayerCode(payer,p):advices.findAll(p);}
    public RemittanceAdvice advice(String n){return advices.findByAdviceNumber(n).orElseThrow(()->new NotFoundException("Advice not found: "+n));}
    public Page<RemittanceLine> exceptions(String payer,Pageable p){return payer==null?lines.findByStatus(LineStatus.UNMATCHED,p):lines.findByStatusAndPayerCode(LineStatus.UNMATCHED,payer,p);}
    public RemittanceLine manualMatch(Long id,ManualMatchRequest request){RemittanceLine l=lines.findById(id).orElseThrow(()->new NotFoundException("Line not found: "+id));OpenInvoice i=invoices.findOpenByNormalizedReference(request.getInvoiceReference().toUpperCase().replaceAll("[^A-Z0-9]","")).orElseThrow(()->new NotFoundException("Invoice not found"));l.setOpenInvoiceId(i.getId());l.setMatchType(MatchType.MANUAL);l.setStatus(LineStatus.MATCHED);i.setOutstandingAmount(BigDecimal.ZERO);i.setStatus(InvoiceStatus.PAID);invoices.save(i);return lines.save(l);}
    public Page<Payer> payers(Pageable p){return payers.findAll(p);}public Payer payer(String c){return payers.findByPayerCode(c).orElseThrow(()->new NotFoundException("Payer not found: "+c));}
    public Payer createPayer(PayerRequest r){Payer p=new Payer();p.setPayerCode(r.getPayerCode());p.setName(r.getName());p.setEmail(r.getEmail());p.setStatus(PayerStatus.ACTIVE);return payers.save(p);}
    public void dispatchPending(){for(PaymentInstruction p:instructions.findByStatus(InstructionStatus.PENDING)){if(cashClient.send(p)){p.setStatus(InstructionStatus.ACKNOWLEDGED);p.setSentAt(LocalDateTime.now());}else{p.setStatus(InstructionStatus.FAILED);p.setFailureReason("Downstream rejected instruction");}instructions.save(p);}}
    private static class ParsedTotals {long trailerCount=-1;BigDecimal amount=BigDecimal.ZERO;BigDecimal trailerAmount=BigDecimal.ZERO;}
}
