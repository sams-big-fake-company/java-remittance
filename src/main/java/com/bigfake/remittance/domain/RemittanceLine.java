package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.LineStatus;
import com.bigfake.remittance.domain.enums.MatchType;
import com.bigfake.remittance.domain.enums.RejectReason;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "remittance_line")
public class RemittanceLine extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remittance_file_id")
    private RemittanceFile remittanceFile;

    private Long lineNumber;
    private String rawRecord;
    private String payerCode;
    private String invoiceReference;
    private BigDecimal paidAmount;
    private BigDecimal invoicedAmount;
    private String deductionReasonCode;
    private LocalDate remittanceDate;

    @Enumerated(EnumType.STRING)
    private LineStatus status;

    @Enumerated(EnumType.STRING)
    private RejectReason rejectReason;

    private String rejectMessage;

    @Enumerated(EnumType.STRING)
    private MatchType matchType;

    private Long openInvoiceId;
    private BigDecimal unappliedAmount = BigDecimal.ZERO;

    public RemittanceFile getRemittanceFile() { return remittanceFile; }
    public void setRemittanceFile(RemittanceFile remittanceFile) { this.remittanceFile = remittanceFile; }
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
    public LineStatus getStatus() { return status; }
    public void setStatus(LineStatus status) { this.status = status; }
    public RejectReason getRejectReason() { return rejectReason; }
    public void setRejectReason(RejectReason rejectReason) { this.rejectReason = rejectReason; }
    public String getRejectMessage() { return rejectMessage; }
    public void setRejectMessage(String rejectMessage) { this.rejectMessage = rejectMessage; }
    public MatchType getMatchType() { return matchType; }
    public void setMatchType(MatchType matchType) { this.matchType = matchType; }
    public Long getOpenInvoiceId() { return openInvoiceId; }
    public void setOpenInvoiceId(Long openInvoiceId) { this.openInvoiceId = openInvoiceId; }
    public BigDecimal getUnappliedAmount() { return unappliedAmount; }
    public void setUnappliedAmount(BigDecimal unappliedAmount) { this.unappliedAmount = unappliedAmount; }
}
