package com.bigfake.remittance.dto;
import lombok.*; @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RejectDto { private Long lineNumber; private String rawRecord; private String reason; private String message; }
