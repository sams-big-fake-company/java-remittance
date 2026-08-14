package com.bigfake.remittance;

import com.bigfake.remittance.domain.OpenInvoice;
import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.domain.enums.FileStatus;
import com.bigfake.remittance.domain.enums.InvoiceStatus;
import com.bigfake.remittance.domain.enums.LineStatus;
import com.bigfake.remittance.repository.OpenInvoiceRepository;
import com.bigfake.remittance.repository.RemittanceFileRepository;
import com.bigfake.remittance.service.RemittanceProcessingService;
import com.bigfake.remittance.dto.FileSummaryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SeededInvoiceIngestTest {

    @Autowired
    private RemittanceProcessingService service;

    @Autowired
    private OpenInvoiceRepository invoices;

    @Autowired
    private RemittanceFileRepository files;

    @Test
    void exactPaymentUpdatesSeededInvoice() {
        FileSummaryDto summary = ingest("seeded-exact.txt", "INV-100045", "1500.00", "1500.00", "");

        RemittanceLine line = onlyLine(summary);
        OpenInvoice invoice = invoice("INV100045");

        assertEquals(FileStatus.PARSED, files.findById(summary.getId()).get().getStatus());
        assertEquals(LineStatus.MATCHED, line.getStatus());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(BigDecimal.ZERO.setScale(2), invoice.getOutstandingAmount());
    }

    @Test
    void shortPaymentUpdatesSeededInvoice() {
        FileSummaryDto summary = ingest("seeded-short.txt", "INV-100046", "2000.00", "2500.00", "SHRT");

        RemittanceLine line = onlyLine(summary);
        OpenInvoice invoice = invoice("INV100046");

        assertEquals(LineStatus.SHORT_PAID, line.getStatus());
        assertEquals("SHRT", line.getDeductionReasonCode());
        assertEquals(InvoiceStatus.PARTIALLY_PAID, invoice.getStatus());
        assertEquals(new BigDecimal("500.00"), invoice.getOutstandingAmount());
    }

    @Test
    void overpaymentUpdatesSeededInvoiceAndRecordsResidual() {
        FileSummaryDto summary = ingest("seeded-overpay.txt", "INV-100047", "800.00", "750.00", "");

        RemittanceLine line = onlyLine(summary);
        OpenInvoice invoice = invoice("INV100047");

        assertEquals(LineStatus.OVERPAID, line.getStatus());
        assertEquals(new BigDecimal("50.00"), line.getUnappliedAmount());
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(BigDecimal.ZERO.setScale(2), invoice.getOutstandingAmount());
    }

    private FileSummaryDto ingest(String fileName, String reference, String paid,
                                  String invoiced, String deduction) {
        String content = "H|BANK|20260810|TX-" + fileName + "|1|" + paid + "\n"
                + "D|ACME000001|" + reference + "|" + paid + "|" + invoiced + "|"
                + deduction + "|20260810|seeded invoice test\n"
                + "T|1|" + paid;
        return service.ingest(fileName, content.getBytes(StandardCharsets.UTF_8));
    }

    private RemittanceLine onlyLine(FileSummaryDto summary) {
        RemittanceFile file = files.findWithLines(summary.getId()).orElseThrow(
                () -> new AssertionError("Ingested file was not found"));
        assertNotNull(file.getLines());
        assertEquals(1, file.getLines().size());
        return file.getLines().get(0);
    }

    private OpenInvoice invoice(String normalizedReference) {
        return invoices.findOpenByNormalizedReference(normalizedReference).orElseThrow(
                () -> new AssertionError("Seeded invoice was not found"));
    }
}
