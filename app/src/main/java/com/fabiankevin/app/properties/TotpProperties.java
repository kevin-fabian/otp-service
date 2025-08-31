package com.fabiankevin.app.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TotpProperties {
    private String algorithm;
    private int digits;
    private int periodSeconds;
    private String issuer;
}
