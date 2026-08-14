package com.bigfake.remittance.service.impl;
import com.bigfake.remittance.domain.OpenInvoice;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.domain.enums.MatchType;
import com.bigfake.remittance.repository.OpenInvoiceRepository;
import com.bigfake.remittance.service.MatchingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class MatchingServiceImpl implements MatchingService {
    private final OpenInvoiceRepository invoices;
    private final BigDecimal minimum;
    private final BigDecimal percent;

    public MatchingServiceImpl(
            OpenInvoiceRepository invoices,
            @Value("${remittance.matching.min-absolute-tolerance:0.50}") BigDecimal minimum,
            @Value("${remittance.matching.tolerance-percent:0.5}") BigDecimal percent) {
        this.invoices = invoices;
        this.minimum = minimum;
        this.percent = percent;
    }

    @Override
    public MatchResult match(RemittanceLine line) {
        String normalized = line.getInvoiceReference() == null
                ? ""
                : line.getInvoiceReference().toUpperCase().replaceAll("[^A-Z0-9]", "");
        Optional<OpenInvoice> exact = invoices.findOpenByNormalizedReference(normalized);
        if (exact.isPresent()) {
            return new MatchResult(exact.get(), MatchType.EXACT_REFERENCE);
        }

        List<OpenInvoice> amountMatches =
                invoices.findExactAmount(line.getPayerCode(), line.getPaidAmount());
        if (amountMatches.size() == 1) {
            return new MatchResult(amountMatches.get(0), MatchType.PAYER_AMOUNT);
        }

        BigDecimal tolerance = minimum.max(line.getPaidAmount()
                .multiply(percent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP));
        List<OpenInvoice> fuzzyMatches = invoices.findWithinAmount(
                line.getPayerCode(),
                line.getPaidAmount().subtract(tolerance),
                line.getPaidAmount().add(tolerance));
        if (fuzzyMatches.size() == 1) {
            return new MatchResult(fuzzyMatches.get(0), MatchType.FUZZY_AMOUNT);
        }
        return new MatchResult(null, MatchType.NONE);
    }
}
