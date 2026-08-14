package com.bigfake.remittance;

import com.bigfake.remittance.controller.WebhookController;
import com.bigfake.remittance.dto.FileSummaryDto;
import com.bigfake.remittance.dto.WebhookRequest;
import com.bigfake.remittance.service.RemittanceProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RemittanceProcessingService service;

    @Test
    void webhookIsReachableWithoutBasicAuth() throws Exception {
        FileSummaryDto summary = new FileSummaryDto(1L, "webhook.txt", "REJECTED",
                "MALFORMED_STRUCTURE", 0L, null);
        when(service.ingest(any(), any())).thenReturn(summary);
        mockMvc.perform(post("/webhooks/bank-remittance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"webhook.txt\",\"contentBase64\":\"%%%\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedWebhookBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/webhooks/bank-remittance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
