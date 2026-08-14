package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.StatusEnums.InvoiceStatus;
import lombok.*;
import javax.persistence.*; import java.math.BigDecimal; import java.time.LocalDate;

@Entity @Table(name="open_invoice") @Getter @Setter @NoArgsConstructor
public class OpenInvoice extends AuditableEntity {
    private String payerCode; private String invoiceReference; private String normalizedReference;
    private BigDecimal invoicedAmount; private BigDecimal outstandingAmount; private LocalDate dueDate;
    @Enumerated(EnumType.STRING) private InvoiceStatus status;
}
