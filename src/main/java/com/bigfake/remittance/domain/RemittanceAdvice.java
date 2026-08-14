package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.StatusEnums.AdviceStatus;
import lombok.*; import javax.persistence.*; import java.math.BigDecimal; import java.time.LocalDate;

@Entity @Table(name="remittance_advice") @Getter @Setter @NoArgsConstructor
public class RemittanceAdvice extends AuditableEntity {
    private String adviceNumber; private Long remittanceFileId; private String payerCode;
    private LocalDate remittanceDate; private BigDecimal totalPaidAmount; private Long lineCount;
    @Enumerated(EnumType.STRING) private AdviceStatus status;
}
