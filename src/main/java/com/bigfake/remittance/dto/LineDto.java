package com.bigfake.remittance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LineDto {
    private Long id;
    private Long lineNumber;
    private String rawRecord;
    private String payerCode;
    private String invoiceReference;
    private BigDecimal paidAmount;
    private BigDecimal invoicedAmount;
    private String deductionReasonCode;
    private LocalDate remittanceDate;
    private String status;
    private String rejectReason;
    private String rejectMessage;
    private String matchType;
    private BigDecimal unappliedAmount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLineNumber() { return lineNumber; }
    public void setLineNumber(Long lineNumber) { this.lineNumber = lineNumber; }
    public String getRawRecord() { return rawRecord; }
    public void setRawRecord(String rawRecord) { this.rawRecord = rawRecord; }
    public String getPayerCode() { return payerCode; }
    public void setPayerCode(String payerCode) { this.payerCode = payerCode; }
    public String getInvoiceReference() { return invoiceReference; }
    public void setInvoiceReference(String invoiceReference) { this.invoiceReference = invoiceReference; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getInvoicedAmount() { return invoicedAmount; }
    public void setInvoicedAmount(BigDecimal invoicedAmount) { this.invoicedAmount = invoicedAmount; }
    public String getDeductionReasonCode() { return deductionReasonCode; }
    public void setDeductionReasonCode(String deductionReasonCode) { this.deductionReasonCode = deductionReasonCode; }
    public LocalDate getRemittanceDate() { return remittanceDate; }
    public void setRemittanceDate(LocalDate remittanceDate) { this.remittanceDate = remittanceDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getRejectMessage() { return rejectMessage; }
    public void setRejectMessage(String rejectMessage) { this.rejectMessage = rejectMessage; }
    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }
    public BigDecimal getUnappliedAmount() { return unappliedAmount; }
    public void setUnappliedAmount(BigDecimal unappliedAmount) { this.unappliedAmount = unappliedAmount; }
}
