package com.bigfake.remittance.dto;
import lombok.*; import javax.validation.constraints.*;
@Getter @Setter @NoArgsConstructor
public class ManualMatchRequest { @NotBlank private String invoiceReference; private String deductionReasonCode; }
