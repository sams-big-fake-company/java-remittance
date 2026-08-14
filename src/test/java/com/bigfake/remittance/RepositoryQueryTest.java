package com.bigfake.remittance;

import com.bigfake.remittance.domain.OpenInvoice;
import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.domain.RemittanceAdvice;
import com.bigfake.remittance.domain.enums.FileStatus;
import com.bigfake.remittance.domain.enums.InvoiceStatus;
import com.bigfake.remittance.domain.enums.LineStatus;
import com.bigfake.remittance.domain.enums.AdviceStatus;
import com.bigfake.remittance.repository.OpenInvoiceRepository;
import com.bigfake.remittance.repository.RemittanceAdviceRepository;
import com.bigfake.remittance.repository.RemittanceFileRepository;
import com.bigfake.remittance.repository.RemittanceLineRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class RepositoryQueryTest {

    @Autowired
    private OpenInvoiceRepository invoices;
    @Autowired
    private RemittanceFileRepository files;
    @Autowired
    private RemittanceLineRepository lines;
    @Autowired
    private RemittanceAdviceRepository advices;

    @Test
    void findsOpenInvoiceByNormalizedReference() {
        OpenInvoice invoice = invoice("QUERY-1", "20.00");
        invoices.save(invoice);
        assertTrue(invoices.findOpenByNormalizedReference("QUERY-1").isPresent());
    }

    @Test
    void findsExactInvoiceAmount() {
        invoices.save(invoice("QUERY-2", "20.00"));
        assertEquals(1, invoices.findExactAmount("ACME000001", new BigDecimal("20.00")).size());
    }

    @Test
    void findsInvoicesWithinNativeAmountRange() {
        invoices.save(invoice("QUERY-3", "20.00"));
        assertEquals(1, invoices.findWithinAmount("ACME000001",
                new BigDecimal("19.00"), new BigDecimal("21.00")).size());
    }

    @Test
    void findsFileWithLinesAndStatusQueries() {
        RemittanceFile file = new RemittanceFile();
        file.setFileName("query.txt");
        file.setChecksum("query-checksum");
        file.setStatus(FileStatus.RECEIVED);
        file = files.save(file);
        RemittanceLine line = new RemittanceLine();
        line.setRemittanceFile(file);
        line.setLineNumber(1L);
        line.setStatus(LineStatus.UNMATCHED);
        lines.save(line);
        assertTrue(files.findWithLines(file.getId()).isPresent());
        assertEquals(1, files.findByStatus(FileStatus.RECEIVED, PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, lines.findByStatus(LineStatus.UNMATCHED, PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void findsAdviceByNumberAndDateCount() {
        RemittanceFile file = new RemittanceFile();
        file.setFileName("advice-file.txt");
        file.setChecksum("advice-file-checksum");
        file.setStatus(FileStatus.RECEIVED);
        file = files.save(file);
        RemittanceAdvice advice = new RemittanceAdvice();
        advice.setAdviceNumber("ADV-20260810-9999");
        advice.setRemittanceFileId(file.getId());
        advice.setPayerCode("ACME000001");
        advice.setRemittanceDate(LocalDate.of(2026, 8, 10));
        advice.setTotalPaidAmount(new BigDecimal("20.00"));
        advice.setLineCount(1L);
        advice.setStatus(AdviceStatus.POSTED);
        advices.save(advice);
        assertTrue(advices.findByAdviceNumber("ADV-20260810-9999").isPresent());
        assertEquals(1, advices.countByRemittanceDate(LocalDate.of(2026, 8, 10)));
    }

    private OpenInvoice invoice(String reference, String amount) {
        OpenInvoice invoice = new OpenInvoice();
        invoice.setPayerCode("ACME000001");
        invoice.setInvoiceReference(reference);
        invoice.setNormalizedReference(reference);
        invoice.setInvoicedAmount(new BigDecimal(amount));
        invoice.setOutstandingAmount(new BigDecimal(amount));
        invoice.setStatus(InvoiceStatus.OPEN);
        return invoice;
    }
}
