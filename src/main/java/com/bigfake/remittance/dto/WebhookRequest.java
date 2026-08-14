package com.bigfake.remittance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WebhookRequest {
    private String senderBankId;
    private String fileName;
    private String format;
    private String contentBase64;
    private String content;
}
