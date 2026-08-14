package com.bigfake.remittance.dto;
import lombok.*; import javax.validation.constraints.*;
@Getter @Setter @NoArgsConstructor
public class PayerRequest { @NotBlank private String payerCode; @NotBlank private String name; private String email; }
