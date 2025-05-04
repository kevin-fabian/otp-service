package com.fabiankevin.app.models;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder(toBuilder = true)
public record Otp(
        UUID id,
        String otpCode,
        String userIdentifier,
        OtpPurpose purpose,
        OtpStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        OffsetDateTime lastAttemptAt,
        DeliveryMethod deliveryMethod,
        int attemptCount,
        String metadata
) {
    public Otp {
        Objects.requireNonNull(otpCode, "otpCode must not be null");
        Objects.requireNonNull(userIdentifier, "userIdentifier must not be null");
        Objects.requireNonNull(purpose, "purpose must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");

        if (otpCode.isBlank()) {
            throw new IllegalArgumentException("otpCode must not be blank");
        }
        if (userIdentifier.isBlank()) {
            throw new IllegalArgumentException("userIdentifier must not be blank");
        }
        if (attemptCount < 0) {
            throw new IllegalArgumentException("attemptCount must not be negative");
        }
    }
}