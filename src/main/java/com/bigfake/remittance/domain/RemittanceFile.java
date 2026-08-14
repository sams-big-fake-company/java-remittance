package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.*;
import com.bigfake.remittance.domain.enums.StatusEnums.FileStatus;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal; import java.time.*;
import java.util.*;

@Entity @Table(name="remittance_file") @Getter @Setter @NoArgsConstructor
public class RemittanceFile extends AuditableEntity {
    private String fileName; private String senderBankId; private LocalDate fileDate;
    private String controlNumber; private String layout; @Column(nullable=false) private String checksum;
    @Enumerated(EnumType.STRING) private FileStatus status; @Enumerated(EnumType.STRING) private RejectReason rejectReason;
    private Long originalFileId; private Long declaredDetailCount; private BigDecimal declaredTotalAmount;
    private Long actualDetailCount; private BigDecimal actualTotalAmount; private LocalDateTime receivedAt;
    @OneToMany(mappedBy="remittanceFile", cascade=CascadeType.ALL) private List<RemittanceLine> lines = new ArrayList<>();
}
