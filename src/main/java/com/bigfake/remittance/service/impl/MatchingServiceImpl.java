package com.bigfake.remittance.service.impl;
import com.bigfake.remittance.domain.*; import com.bigfake.remittance.domain.enums.StatusEnums.*; import com.bigfake.remittance.repository.OpenInvoiceRepository;
import com.bigfake.remittance.service.MatchingService; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import java.math.*; import java.util.*;
@Service
public class MatchingServiceImpl implements MatchingService {
    private final OpenInvoiceRepository invoices; private final BigDecimal minimum; private final BigDecimal percent;
    public MatchingServiceImpl(OpenInvoiceRepository invoices, @Value("${remittance.matching.min-absolute-tolerance:0.50}") BigDecimal minimum,
                               @Value("${remittance.matching.tolerance-percent:0.5}") BigDecimal percent) { this.invoices=invoices; this.minimum=minimum; this.percent=percent; }
    public MatchResult match(RemittanceLine line) {
        String normalized=line.getInvoiceReference()==null?"":line.getInvoiceReference().toUpperCase().replaceAll("[^A-Z0-9]","");
        Optional<OpenInvoice> exact=invoices.findOpenByNormalizedReference(normalized);
        if(exact.isPresent()) return new MatchResult(exact.get(),MatchType.EXACT_REFERENCE);
        List<OpenInvoice> amount=invoices.findExactAmount(line.getPayerCode(),line.getPaidAmount());
        if(amount.size()==1) return new MatchResult(amount.get(0),MatchType.PAYER_AMOUNT);
        BigDecimal tolerance=minimum.max(line.getPaidAmount().multiply(percent).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP));
        List<OpenInvoice> fuzzy=invoices.findWithinAmount(line.getPayerCode(),line.getPaidAmount().subtract(tolerance),line.getPaidAmount().add(tolerance));
        return fuzzy.size()==1?new MatchResult(fuzzy.get(0),MatchType.FUZZY_AMOUNT):new MatchResult(null,MatchType.NONE);
    }
}
