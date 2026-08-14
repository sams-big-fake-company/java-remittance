package com.bigfake.remittance.domain;

import lombok.*;
import javax.persistence.*;

@Entity @Table(name="deduction_reason_code") @Getter @Setter @NoArgsConstructor
public class DeductionReasonCode {
    @Id private String code; private String description;
    @Column(nullable=false) private Boolean active;
}
