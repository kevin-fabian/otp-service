package com.fabiankevin.app.models;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import lombok.Builder;
import lombok.With;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.fabiankevin.app.models.enums.OtpStatus.ACTIVE;

@Builder(toBuilder = true)
@With
public record OtpTransaction(
        UUID id,
        String otpCode,
        String recipient,
        OtpPurpose purpose,
        OtpStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt,
        OffsetDateTime updatedAt,
        DeliveryMethod deliveryMethod,
        int attemptCount,
        String metadata
) {
    public OtpTransaction {
        Objects.requireNonNull(otpCode, "otpCode must not be null");
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(purpose, "purpose must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");

        if (otpCode.isBlank()) {
            throw new IllegalArgumentException("otpCode must not be blank");
        }
        if (recipient.isBlank()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
        if (attemptCount < 0) {
            throw new IllegalArgumentException("attemptCount must not be negative");
        }
    }

    public boolean isActive(){
        return ACTIVE == status;
    }
}