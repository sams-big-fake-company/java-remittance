package com.bigfake.remittance.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "deduction_reason_code")
public class DeductionReasonCode {
    @Id
    private String code;

    private String description;

    @Column(nullable = false)
    private Boolean active;

    public DeductionReasonCode() {
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
