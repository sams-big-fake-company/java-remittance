package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.StatusEnums.InstructionStatus;
import lombok.*; import javax.persistence.*; import java.math.BigDecimal; import java.time.LocalDateTime;

@Entity @Table(name="payment_instruction") @Getter @Setter @NoArgsConstructor
public class PaymentInstruction extends AuditableEntity {
    private String instructionReference; private Long remittanceAdviceId; private String payerCode;
    private BigDecimal amount; @Enumerated(EnumType.STRING) private InstructionStatus status;
    private LocalDateTime sentAt; private String failureReason;
}
