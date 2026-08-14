package com.bigfake.remittance;

import com.bigfake.remittance.config.SecurityConfig;
import com.bigfake.remittance.controller.RemittanceController;
import com.bigfake.remittance.dto.FileSummaryDto;
import com.bigfake.remittance.dto.PayerRequest;
import com.bigfake.remittance.mapper.PayerMapper;
import com.bigfake.remittance.mapper.RemittanceAdviceMapper;
import com.bigfake.remittance.mapper.RemittanceFileMapper;
import com.bigfake.remittance.mapper.RemittanceLineMapper;
import com.bigfake.remittance.service.RemittanceProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RemittanceController.class)
@Import(SecurityConfig.class)
class RemittanceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private RemittanceProcessingService service;
    @MockBean
    private RemittanceFileMapper fileMapper;
    @MockBean
    private RemittanceLineMapper lineMapper;
    @MockBean
    private RemittanceAdviceMapper adviceMapper;
    @MockBean
    private PayerMapper payerMapper;

    @Test
    void uploadRequiresAuthentication() throws Exception {
        mockMvc.perform(multipart("/api/v1/remittance-files")
                        .file(new MockMultipartFile("file", "file.txt", "text/plain", "x".getBytes())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadReturnsCreatedWithCredentials() throws Exception {
        when(service.ingest(eq("file.txt"), any())).thenReturn(
                new FileSummaryDto(1L, "file.txt", "PARSED", null, 1L, null));
        mockMvc.perform(multipart("/api/v1/remittance-files")
                        .file(new MockMultipartFile("file", "file.txt", "text/plain", "x".getBytes()))
                        .with(httpBasic("admin", "changeit")))
                .andExpect(status().isCreated());
    }

    @Test
    void missingUploadPartReturnsBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/remittance-files")
                        .with(httpBasic("admin", "changeit")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidPayerBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/payers")
                        .with(httpBasic("admin", "changeit"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"payerCode\":\"\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownPayerReturnsNotFound() throws Exception {
        when(service.payer("UNKNOWN"))
                .thenThrow(new com.bigfake.remittance.exception.NotFoundException("Payer not found"));
        mockMvc.perform(get("/api/v1/payers/UNKNOWN").with(httpBasic("admin", "changeit")))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/remittance-files/1/rejects")
                        .with(httpBasic("admin", "changeit")))
                .andExpect(status().isOk());
    }
}
