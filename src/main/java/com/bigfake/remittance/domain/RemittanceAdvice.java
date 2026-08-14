package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.AdviceStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "remittance_advice")
public class RemittanceAdvice extends AuditableEntity {
    private String adviceNumber;
    private Long remittanceFileId;
    private String payerCode;
    private LocalDate remittanceDate;
    private BigDecimal totalPaidAmount;
    private Long lineCount;

    @Enumerated(EnumType.STRING)
    private AdviceStatus status;

    public String getAdviceNumber() { return adviceNumber; }
    public void setAdviceNumber(String adviceNumber) { this.adviceNumber = adviceNumber; }
    public Long getRemittanceFileId() { return remittanceFileId; }
    public void setRemittanceFileId(Long remittanceFileId) { this.remittanceFileId = remittanceFileId; }
    public String getPayerCode() { return payerCode; }
    public void setPayerCode(String payerCode) { this.payerCode = payerCode; }
    public LocalDate getRemittanceDate() { return remittanceDate; }
    public void setRemittanceDate(LocalDate remittanceDate) { this.remittanceDate = remittanceDate; }
    public BigDecimal getTotalPaidAmount() { return totalPaidAmount; }
    public void setTotalPaidAmount(BigDecimal totalPaidAmount) { this.totalPaidAmount = totalPaidAmount; }
    public Long getLineCount() { return lineCount; }
    public void setLineCount(Long lineCount) { this.lineCount = lineCount; }
    public AdviceStatus getStatus() { return status; }
    public void setStatus(AdviceStatus status) { this.status = status; }
}
