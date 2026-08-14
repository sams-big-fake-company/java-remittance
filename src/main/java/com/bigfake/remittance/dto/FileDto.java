package com.bigfake.remittance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FileDto {
    private Long id;
    private String fileName;
    private String senderBankId;
    private LocalDate fileDate;
    private String controlNumber;
    private String layout;
    private String status;
    private String rejectReason;
    private String rejectMessage;
    private Long declaredDetailCount;
    private BigDecimal declaredTotalAmount;
    private Long actualDetailCount;
    private BigDecimal actualTotalAmount;
    private LocalDateTime receivedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getSenderBankId() { return senderBankId; }
    public void setSenderBankId(String senderBankId) { this.senderBankId = senderBankId; }
    public LocalDate getFileDate() { return fileDate; }
    public void setFileDate(LocalDate fileDate) { this.fileDate = fileDate; }
    public String getControlNumber() { return controlNumber; }
    public void setControlNumber(String controlNumber) { this.controlNumber = controlNumber; }
    public String getLayout() { return layout; }
    public void setLayout(String layout) { this.layout = layout; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public String getRejectMessage() { return rejectMessage; }
    public void setRejectMessage(String rejectMessage) { this.rejectMessage = rejectMessage; }
    public Long getDeclaredDetailCount() { return declaredDetailCount; }
    public void setDeclaredDetailCount(Long declaredDetailCount) { this.declaredDetailCount = declaredDetailCount; }
    public BigDecimal getDeclaredTotalAmount() { return declaredTotalAmount; }
    public void setDeclaredTotalAmount(BigDecimal declaredTotalAmount) { this.declaredTotalAmount = declaredTotalAmount; }
    public Long getActualDetailCount() { return actualDetailCount; }
    public void setActualDetailCount(Long actualDetailCount) { this.actualDetailCount = actualDetailCount; }
    public BigDecimal getActualTotalAmount() { return actualTotalAmount; }
    public void setActualTotalAmount(BigDecimal actualTotalAmount) { this.actualTotalAmount = actualTotalAmount; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
}
