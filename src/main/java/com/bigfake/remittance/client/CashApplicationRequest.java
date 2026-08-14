package com.bigfake.remittance.client;

import java.math.BigDecimal;

public class CashApplicationRequest {
    private String instructionReference;
    private String payerCode;
    private BigDecimal amount;

    public CashApplicationRequest() {
    }

    public CashApplicationRequest(String instructionReference, String payerCode, BigDecimal amount) {
        this.instructionReference = instructionReference;
        this.payerCode = payerCode;
        this.amount = amount;
    }

    public String getInstructionReference() { return instructionReference; }
    public void setInstructionReference(String instructionReference) { this.instructionReference = instructionReference; }
    public String getPayerCode() { return payerCode; }
    public void setPayerCode(String payerCode) { this.payerCode = payerCode; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
