package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
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
        Instant updatedAt,
        @Schema(description = "Date and time when OTP will expire", example = "2024-03-01T12:00:00.000+00:00")
        OffsetDateTime expiredAt) {

    public static OtpResponse from(OneTimePasswordTransaction oneTimePasswordTransaction) {
        return OtpResponse.builder()
                .id(oneTimePasswordTransaction.id())
                .recipient(oneTimePasswordTransaction.recipient())
                .purpose(oneTimePasswordTransaction.purpose())
                .status(oneTimePasswordTransaction.status())
                .deliveryMethod(oneTimePasswordTransaction.deliveryMethod())
                .createdAt(oneTimePasswordTransaction.createdAt())
                .updatedAt(oneTimePasswordTransaction.updatedAt())
                .expiredAt(oneTimePasswordTransaction.expiresAt())
                .metadata(oneTimePasswordTransaction.metadata())
                .build();
    }
}