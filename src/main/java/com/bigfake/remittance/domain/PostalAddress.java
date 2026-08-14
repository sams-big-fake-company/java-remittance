package com.bigfake.remittance.domain;

import lombok.*;
import javax.persistence.Embeddable;

@Embeddable @Getter @Setter @NoArgsConstructor
public class PostalAddress {
    private String addressLine1; private String addressLine2; private String city;
    private String state; private String postalCode; private String country;
}
