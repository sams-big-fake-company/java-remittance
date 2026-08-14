package com.bigfake.remittance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class ManualMatchRequest {
    @NotBlank
    private String invoiceReference;
    private String deductionReasonCode;
}
