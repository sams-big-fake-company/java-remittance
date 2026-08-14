package com.bigfake.remittance.controller;

import com.bigfake.remittance.dto.AdviceDto;
import com.bigfake.remittance.dto.FileDto;
import com.bigfake.remittance.dto.FileSummaryDto;
import com.bigfake.remittance.dto.LineDto;
import com.bigfake.remittance.dto.ManualMatchRequest;
import com.bigfake.remittance.dto.PayerDto;
import com.bigfake.remittance.dto.PayerRequest;
import com.bigfake.remittance.dto.RejectDto;
import com.bigfake.remittance.mapper.PayerMapper;
import com.bigfake.remittance.mapper.RemittanceAdviceMapper;
import com.bigfake.remittance.mapper.RemittanceFileMapper;
import com.bigfake.remittance.mapper.RemittanceLineMapper;
import com.bigfake.remittance.service.RemittanceProcessingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RemittanceController {
    private final RemittanceProcessingService service;
    private final RemittanceFileMapper fileMapper;
    private final RemittanceLineMapper lineMapper;
    private final RemittanceAdviceMapper adviceMapper;
    private final PayerMapper payerMapper;

    public RemittanceController(RemittanceProcessingService service, RemittanceFileMapper fileMapper,
                                RemittanceLineMapper lineMapper, RemittanceAdviceMapper adviceMapper,
                                PayerMapper payerMapper) {
        this.service = service;
        this.fileMapper = fileMapper;
        this.lineMapper = lineMapper;
        this.adviceMapper = adviceMapper;
        this.payerMapper = payerMapper;
    }

    @PostMapping("/remittance-files")
    public ResponseEntity<FileSummaryDto> upload(@RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.ingest(file.getOriginalFilename(), file.getBytes()));
    }

    @GetMapping("/remittance-files")
    public Page<FileDto> files(@RequestParam(required = false) String status, Pageable page) {
        return service.listFiles(status, page).map(fileMapper::toDto);
    }

    @GetMapping("/remittance-files/{id}")
    public FileDto file(@PathVariable Long id) {
        return fileMapper.toDto(service.getFile(id));
    }

    @GetMapping("/remittance-files/{id}/rejects")
    public List<RejectDto> rejects(@PathVariable Long id) {
        return service.rejects(id);
    }

    @PostMapping("/remittance-files/{id}/post")
    public FileDto post(@PathVariable Long id) {
        return fileMapper.toDto(service.post(id));
    }

    @GetMapping("/advices")
    public Page<AdviceDto> advices(@RequestParam(required = false) String payerCode,
                                   @RequestParam(required = false) String status, Pageable page) {
        return service.listAdvices(payerCode, status, page).map(adviceMapper::toDto);
    }

    @GetMapping("/advices/{number}")
    public AdviceDto advice(@PathVariable String number) {
        return adviceMapper.toDto(service.advice(number));
    }

    @GetMapping("/exceptions")
    public Page<LineDto> exceptions(@RequestParam(required = false) String payerCode, Pageable page) {
        return service.exceptions(payerCode, page).map(lineMapper::toDto);
    }

    @PostMapping("/exceptions/{lineId}/match")
    public LineDto match(@PathVariable Long lineId, @Valid @RequestBody ManualMatchRequest request) {
        return lineMapper.toDto(service.manualMatch(lineId, request));
    }

    @GetMapping("/payers")
    public Page<PayerDto> payers(Pageable page) {
        return service.payers(page).map(payerMapper::toDto);
    }

    @GetMapping("/payers/{code}")
    public PayerDto payer(@PathVariable String code) {
        return payerMapper.toDto(service.payer(code));
    }

    @PostMapping("/payers")
    public ResponseEntity<PayerDto> create(@Valid @RequestBody PayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payerMapper.toDto(service.createPayer(request)));
    }
}
