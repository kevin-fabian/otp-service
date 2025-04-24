package com.fabiankevin.app.models;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
@Builder(toBuilder = true)
public class Otp {
    private final UUID id;
    private final String otpCode;
    private final String userIdentifier;
    private final OtpPurpose purpose;
    private final OtpStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final DeliveryMethod deliveryMethod;
    private final int attemptCount;
    private final String metadata;

    // Private constructor for validation
    private Otp(UUID id, String otpCode, String userIdentifier, OtpPurpose purpose, OtpStatus status,
                LocalDateTime createdAt, LocalDateTime expiresAt, DeliveryMethod deliveryMethod,
                String deliveryTarget, int attemptCount, String metadata) {
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
        if (expiresAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("expiresAt must not be before createdAt");
        }

        this.id = id;
        this.otpCode = otpCode;
        this.userIdentifier = userIdentifier;
        this.purpose = purpose;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.deliveryMethod = deliveryMethod;
        this.attemptCount = attemptCount;
        this.metadata = metadata;
    }
}
