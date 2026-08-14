package com.bigfake.remittance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectDto {
    private Long lineNumber;
    private String rawRecord;
    private String reason;
    private String message;
}
