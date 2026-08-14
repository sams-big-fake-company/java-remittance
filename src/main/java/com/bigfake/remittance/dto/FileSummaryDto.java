package com.bigfake.remittance.dto;
import lombok.*; import java.math.BigDecimal;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FileSummaryDto { private Long id; private String fileName; private String status; private String rejectReason; private long lineCount; private BigDecimal totalAmount; }
