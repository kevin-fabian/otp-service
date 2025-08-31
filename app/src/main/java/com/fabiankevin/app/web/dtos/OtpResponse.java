package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record OtpResponse(
        UUID id,
        String recipient,
        OtpPurpose purpose,
        OtpStatus status,
        DeliveryMethod deliveryMethod,
        String metadata,
        Instant createdAt,
        OffsetDateTime updatedAt,
        @Schema(description = "Date and time when OTP will expire", example = "2024-03-01T12:00:00.000+00:00")
        OffsetDateTime expiredAt) {

    public static OtpResponse from(OtpTransaction otpTransaction) {
        return OtpResponse.builder()
                .id(otpTransaction.id())
                .recipient(otpTransaction.recipient())
                .purpose(otpTransaction.purpose())
                .status(otpTransaction.status())
                .deliveryMethod(otpTransaction.deliveryMethod())
                .createdAt(otpTransaction.createdAt().toInstant())
                .updatedAt(otpTransaction.updatedAt())
                .expiredAt(otpTransaction.expiresAt())
                .metadata(otpTransaction.metadata())
                .build();
    }
}