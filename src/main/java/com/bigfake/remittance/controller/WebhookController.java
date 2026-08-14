package com.bigfake.remittance.controller;
import com.bigfake.remittance.dto.*; import com.bigfake.remittance.service.RemittanceProcessingService; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.nio.charset.StandardCharsets; import java.util.Base64;
@RestController @RequestMapping("/webhooks")
public class WebhookController {
 private final RemittanceProcessingService service; public WebhookController(RemittanceProcessingService service){this.service=service;}
 @PostMapping("/bank-remittance") public ResponseEntity<?> receive(@RequestBody WebhookRequest request){byte[] content=request.getContent()!=null?request.getContent().getBytes(StandardCharsets.UTF_8):Base64.getDecoder().decode(request.getContentBase64());return ResponseEntity.status(HttpStatus.CREATED).body(service.ingest(request.getFileName(),content));}
}
