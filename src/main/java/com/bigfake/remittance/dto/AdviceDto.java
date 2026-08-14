package com.bigfake.remittance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AdviceDto {
    private Long id;
    private String adviceNumber;
    private Long remittanceFileId;
    private String payerCode;
    private LocalDate remittanceDate;
    private BigDecimal totalPaidAmount;
    private Long lineCount;
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
