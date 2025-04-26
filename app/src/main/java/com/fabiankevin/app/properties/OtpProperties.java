package com.fabiankevin.app.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OtpProperties {
    private int maxAttempt;
    private int otpLength;
    private int expiresInMinutes;
}
