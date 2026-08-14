package com.bigfake.remittance.controller;
import com.bigfake.remittance.domain.*; import com.bigfake.remittance.dto.*; import com.bigfake.remittance.service.RemittanceProcessingService;
import org.springframework.data.domain.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid; import java.io.IOException; import java.util.*;
@RestController @RequestMapping("/api/v1")
public class RemittanceController {
    private final RemittanceProcessingService service; public RemittanceController(RemittanceProcessingService service){this.service=service;}
    @PostMapping("/remittance-files") public ResponseEntity<FileSummaryDto> upload(@RequestParam("file") MultipartFile file)throws IOException{return ResponseEntity.status(HttpStatus.CREATED).body(service.ingest(file.getOriginalFilename(),file.getBytes()));}
    @GetMapping("/remittance-files") public Page<RemittanceFile> files(@RequestParam(required=false)String status,Pageable page){return service.listFiles(status,page);}
    @GetMapping("/remittance-files/{id}") public RemittanceFile file(@PathVariable Long id){return service.getFile(id);}
    @GetMapping("/remittance-files/{id}/rejects") public List<RejectDto> rejects(@PathVariable Long id){return service.rejects(id);}
    @PostMapping("/remittance-files/{id}/post") public RemittanceFile post(@PathVariable Long id){return service.post(id);}
    @GetMapping("/advices") public Page<RemittanceAdvice> advices(@RequestParam(required=false)String payerCode,@RequestParam(required=false)String status,Pageable page){return service.listAdvices(payerCode,status,page);}
    @GetMapping("/advices/{number}") public RemittanceAdvice advice(@PathVariable String number){return service.advice(number);}
    @GetMapping("/exceptions") public Page<RemittanceLine> exceptions(@RequestParam(required=false)String payerCode,Pageable page){return service.exceptions(payerCode,page);}
    @PostMapping("/exceptions/{lineId}/match") public RemittanceLine match(@PathVariable Long lineId,@Valid @RequestBody ManualMatchRequest r){return service.manualMatch(lineId,r);}
    @GetMapping("/payers") public Page<Payer> payers(Pageable page){return service.payers(page);}
    @GetMapping("/payers/{code}") public Payer payer(@PathVariable String code){return service.payer(code);}
    @PostMapping("/payers") public ResponseEntity<Payer> create(@Valid @RequestBody PayerRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.createPayer(r));}
}
