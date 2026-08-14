package com.bigfake.remittance.client;

public class CashApplicationResponse {
    private boolean accepted;
    private String message;

    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
