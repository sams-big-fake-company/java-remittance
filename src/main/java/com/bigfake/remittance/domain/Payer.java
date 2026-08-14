package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.PayerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "payer")
public class Payer extends AuditableEntity {
    @Column(nullable = false, unique = true)
    private String payerCode;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayerStatus status;

    private String email;

    @Embedded
    private PostalAddress address;

    public String getPayerCode() { return payerCode; }
    public void setPayerCode(String payerCode) { this.payerCode = payerCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PayerStatus getStatus() { return status; }
    public void setStatus(PayerStatus status) { this.status = status; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public PostalAddress getAddress() { return address; }
    public void setAddress(PostalAddress address) { this.address = address; }
}
