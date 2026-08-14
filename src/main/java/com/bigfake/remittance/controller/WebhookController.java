package com.bigfake.remittance.controller;

import com.bigfake.remittance.dto.FileSummaryDto;
import com.bigfake.remittance.dto.WebhookRequest;
import com.bigfake.remittance.service.RemittanceProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
 private final RemittanceProcessingService service;

 public WebhookController(RemittanceProcessingService service) {
     this.service = service;
 }

 @PostMapping("/bank-remittance")
    public ResponseEntity<FileSummaryDto> receive(@RequestBody WebhookRequest request) {
        if (request.getContent() == null && request.getContentBase64() == null) {
            throw new IllegalArgumentException("Webhook content is required");
        }
        byte[] content = request.getContent() != null ? request.getContent().getBytes(StandardCharsets.UTF_8)
                : Base64.getDecoder().decode(request.getContentBase64());
     return ResponseEntity.status(HttpStatus.CREATED).body(service.ingest(request.getFileName(), content));
 }
}
