package com.fabiankevin.app.services.totp.commands;

import lombok.Builder;

import java.time.Duration;

@Builder
public record GenerateQrCodeCommand(
        String label,
        String algorithm,
        String secret,
        String issuer,
        int digits,
        Duration period
) {
}
