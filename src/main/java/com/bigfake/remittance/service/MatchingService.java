package com.bigfake.remittance.service;
import com.bigfake.remittance.domain.*; import com.bigfake.remittance.domain.enums.StatusEnums.MatchType;
public interface MatchingService { MatchResult match(RemittanceLine line); class MatchResult {
    public final OpenInvoice invoice; public final MatchType type;
    public MatchResult(OpenInvoice invoice, MatchType type) { this.invoice=invoice; this.type=type; }
} }
