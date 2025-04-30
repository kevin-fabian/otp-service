package com.fabiankevin.app.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OtpProperties {
    private int maxAttempts;
    private int codeLength;
    private int expirationMinutes;
}
