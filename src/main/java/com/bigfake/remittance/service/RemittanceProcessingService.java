package com.bigfake.remittance.service;

import com.bigfake.remittance.domain.Payer;
import com.bigfake.remittance.domain.RemittanceAdvice;
import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.dto.FileSummaryDto;
import com.bigfake.remittance.dto.ManualMatchRequest;
import com.bigfake.remittance.dto.PayerRequest;
import com.bigfake.remittance.dto.RejectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RemittanceProcessingService {
    FileSummaryDto ingest(String fileName, byte[] content);
    Page<RemittanceFile> listFiles(String status, Pageable pageable);
    RemittanceFile getFile(Long id);
    List<RejectDto> rejects(Long id);
    RemittanceFile post(Long id);
    Page<RemittanceAdvice> listAdvices(String payer, String status, Pageable pageable);
    RemittanceAdvice advice(String number);
    Page<RemittanceLine> exceptions(String payer, Pageable pageable);
    RemittanceLine manualMatch(Long id, ManualMatchRequest request);
    Page<Payer> payers(Pageable pageable);
    Payer payer(String code);
    Payer createPayer(PayerRequest request);
    void dispatchPending();
}
