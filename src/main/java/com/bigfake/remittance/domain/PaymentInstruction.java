package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.InstructionStatus;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_instruction")
public class PaymentInstruction extends AuditableEntity {
    private String instructionReference;
    private Long remittanceAdviceId;
    private String payerCode;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private InstructionStatus status;

    private LocalDateTime sentAt;
    private String failureReason;

    public String getInstructionReference() { return instructionReference; }
    public void setInstructionReference(String instructionReference) { this.instructionReference = instructionReference; }
    public Long getRemittanceAdviceId() { return remittanceAdviceId; }
    public void setRemittanceAdviceId(Long remittanceAdviceId) { this.remittanceAdviceId = remittanceAdviceId; }
    public String getPayerCode() { return payerCode; }
    public void setPayerCode(String payerCode) { this.payerCode = payerCode; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public InstructionStatus getStatus() { return status; }
    public void setStatus(InstructionStatus status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
