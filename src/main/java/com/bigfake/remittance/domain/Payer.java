package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.StatusEnums.PayerStatus;
import lombok.*;
import javax.persistence.*;

@Entity @Table(name="payer") @Getter @Setter @NoArgsConstructor
public class Payer extends AuditableEntity {
    @Column(nullable=false, unique=true) private String payerCode;
    @Column(nullable=false) private String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private PayerStatus status;
    private String email;
    @Embedded private PostalAddress address;
}
