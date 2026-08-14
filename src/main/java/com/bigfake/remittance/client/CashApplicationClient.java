package com.bigfake.remittance.client;

import com.bigfake.remittance.domain.PaymentInstruction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CashApplicationClient {

    private final RestTemplate restTemplate;
    private final String endpoint;

    public CashApplicationClient(
            @Value("${remittance.cash-application.base-url:http://localhost:9090}") String baseUrl,
            @Value("${remittance.cash-application.path:/cash-application/instructions}") String path) {
        this.restTemplate = new RestTemplate();
        this.endpoint = baseUrl + path;
    }

    public boolean send(PaymentInstruction instruction) {
        CashApplicationRequest request = new CashApplicationRequest(
                instruction.getInstructionReference(),
                instruction.getPayerCode(),
                instruction.getAmount());

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                log.info("Sending payment instruction "
                        + instruction.getInstructionReference() + " attempt " + attempt);
                ResponseEntity<CashApplicationResponse> response = restTemplate.postForEntity(
                        endpoint, request, CashApplicationResponse.class);
                if (response.getBody() != null && response.getBody().isAccepted()) {
                    return true;
                }
                log.warn("Cash application rejected instruction "
                        + instruction.getInstructionReference());
            } catch (RestClientException exception) {
                log.warn("Downstream send failed " + exception.getMessage());
                if (attempt == 3) {
                    return false;
                }
            // FIXME REM-5709: add circuit breaker and proper timeout // NOSONAR
                try {
                    Thread.sleep(100L * attempt);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }
}
