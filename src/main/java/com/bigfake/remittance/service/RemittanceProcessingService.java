package com.bigfake.remittance.service;
import com.bigfake.remittance.domain.*; import com.bigfake.remittance.dto.*; import org.springframework.data.domain.*; import java.util.*;
public interface RemittanceProcessingService {
    FileSummaryDto ingest(String fileName, byte[] content);
    Page<RemittanceFile> listFiles(String status, Pageable pageable); RemittanceFile getFile(Long id);
    List<RejectDto> rejects(Long id); RemittanceFile post(Long id);
    Page<RemittanceAdvice> listAdvices(String payer, String status, Pageable pageable); RemittanceAdvice advice(String number);
    Page<RemittanceLine> exceptions(String payer, Pageable pageable); RemittanceLine manualMatch(Long id, ManualMatchRequest request);
    Page<Payer> payers(Pageable pageable); Payer payer(String code); Payer createPayer(PayerRequest request);
    void dispatchPending();
}
