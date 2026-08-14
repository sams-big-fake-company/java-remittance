package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.*;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal; import java.time.LocalDate;

@Entity @Table(name="remittance_line") @Getter @Setter @NoArgsConstructor
public class RemittanceLine extends AuditableEntity {
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="remittance_file_id") private RemittanceFile remittanceFile;
    private Long lineNumber; private String rawRecord; private String payerCode; private String invoiceReference;
    private BigDecimal paidAmount; private BigDecimal invoicedAmount; private String deductionReasonCode;
    private LocalDate remittanceDate; @Enumerated(EnumType.STRING) private StatusEnums.LineStatus status;
    @Enumerated(EnumType.STRING) private RejectReason rejectReason; private String rejectMessage;
    @Enumerated(EnumType.STRING) private StatusEnums.MatchType matchType; private Long openInvoiceId;
    private BigDecimal unappliedAmount = BigDecimal.ZERO;
}
