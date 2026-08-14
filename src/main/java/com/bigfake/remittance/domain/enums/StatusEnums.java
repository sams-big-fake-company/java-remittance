package com.bigfake.remittance.domain.enums;

public final class StatusEnums {
    private StatusEnums() {}
    public enum PayerStatus { ACTIVE, INACTIVE }
    public enum FileStatus { RECEIVED, PARSED, REJECTED, POSTED }
    public enum LineStatus { ACCEPTED, REJECTED, MATCHED, SHORT_PAID, OVERPAID, UNMATCHED }
    public enum InvoiceStatus { OPEN, PARTIALLY_PAID, PAID, CLOSED }
    public enum MatchType { EXACT_REFERENCE, PAYER_AMOUNT, FUZZY_AMOUNT, MANUAL, NONE }
    public enum AdviceStatus { DRAFT, POSTED, PARTIALLY_POSTED, CANCELLED }
    public enum InstructionStatus { PENDING, SENT, ACKNOWLEDGED, FAILED }
}
