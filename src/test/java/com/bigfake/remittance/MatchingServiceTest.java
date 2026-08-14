package com.bigfake.remittance;

import com.bigfake.remittance.domain.OpenInvoice;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.domain.enums.StatusEnums.MatchType;
import com.bigfake.remittance.repository.OpenInvoiceRepository;
import com.bigfake.remittance.service.MatchingService;
import com.bigfake.remittance.service.impl.MatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {
    @Mock private OpenInvoiceRepository repository;
    private MatchingService matching;

    @BeforeEach
    void setUp() {
        matching = new MatchingServiceImpl(repository, new BigDecimal("0.50"), new BigDecimal("0.5"));
    }

    @Test
    void exactReferenceWinsBeforeAmountSearch() {
        RemittanceLine line = line("ACME000001", "inv-100045", "1500.00");
        OpenInvoice invoice = invoice("INV100045", "1500.00");
        when(repository.findOpenByNormalizedReference("INV100045")).thenReturn(Optional.of(invoice));

        MatchingService.MatchResult result = matching.match(line);

        assertSame(invoice, result.invoice);
        assertEquals(MatchType.EXACT_REFERENCE, result.type);
        verify(repository, never()).findExactAmount(anyString(), any());
    }

    @Test
    void exactPayerAmountMatchesOneInvoice() {
        RemittanceLine line = line("ACME000001", "unhelpful-ref", "750.00");
        OpenInvoice invoice = invoice("INV100047", "750.00");
        when(repository.findOpenByNormalizedReference("UNHELPFULREF")).thenReturn(Optional.empty());
        when(repository.findExactAmount("ACME000001", new BigDecimal("750.00")))
                .thenReturn(Collections.singletonList(invoice));

        MatchingService.MatchResult result = matching.match(line);

        assertSame(invoice, result.invoice);
        assertEquals(MatchType.PAYER_AMOUNT, result.type);
    }

    @Test
    void ambiguousExactAmountsFallThroughToFuzzyTier() {
        RemittanceLine line = line("ACME000001", "unhelpful-ref", "100.00");
        OpenInvoice first = invoice("INV-A", "99.70");
        OpenInvoice second = invoice("INV-B", "100.30");
        when(repository.findOpenByNormalizedReference("UNHELPFULREF")).thenReturn(Optional.empty());
        when(repository.findExactAmount("ACME000001", new BigDecimal("100.00")))
                .thenReturn(Arrays.asList(first, second));
        when(repository.findWithinAmount("ACME000001", new BigDecimal("99.50"), new BigDecimal("100.50")))
                .thenReturn(Collections.singletonList(first));

        MatchingService.MatchResult result = matching.match(line);

        assertSame(first, result.invoice);
        assertEquals(MatchType.FUZZY_AMOUNT, result.type);
        verify(repository).findWithinAmount("ACME000001", new BigDecimal("99.50"), new BigDecimal("100.50"));
    }

    @Test
    void fuzzyAmbiguityProducesNoMatch() {
        RemittanceLine line = line("ACME000001", "unhelpful-ref", "100.00");
        when(repository.findOpenByNormalizedReference("UNHELPFULREF")).thenReturn(Optional.empty());
        when(repository.findExactAmount("ACME000001", new BigDecimal("100.00"))).thenReturn(Collections.emptyList());
        when(repository.findWithinAmount("ACME000001", new BigDecimal("99.50"), new BigDecimal("100.50")))
                .thenReturn(Arrays.asList(invoice("INV-A", "99.70"), invoice("INV-B", "100.30")));

        MatchingService.MatchResult result = matching.match(line);

        assertNull(result.invoice);
        assertEquals(MatchType.NONE, result.type);
    }

    private RemittanceLine line(String payer, String reference, String amount) {
        RemittanceLine line = new RemittanceLine();
        line.setPayerCode(payer);
        line.setInvoiceReference(reference);
        line.setPaidAmount(new BigDecimal(amount));
        return line;
    }

    private OpenInvoice invoice(String reference, String amount) {
        OpenInvoice invoice = new OpenInvoice();
        invoice.setInvoiceReference(reference);
        invoice.setNormalizedReference(reference.replaceAll("[^A-Za-z0-9]", "").toUpperCase());
        invoice.setOutstandingAmount(new BigDecimal(amount));
        return invoice;
    }
}
