package com.fabiankevin.app.services.commands;

import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record GenerateOtpCommand(
        String userIdentifier,
        OtpPurpose purpose,
        OtpStatus status,
        DeliveryMethod deliveryMethod,
        String deliveryTarget,
        String metadata
) {

    public Otp toModel() {
        LocalDateTime now = LocalDateTime.now();

        return Otp.builder()
                .otpCode(null)
                .userIdentifier(userIdentifier)
                .purpose(purpose)
                .status(status)
                .createdAt(now)
                .expiresAt(now.plusMinutes(5))
                .deliveryMethod(deliveryMethod)
                .attemptCount(0)
                .metadata(metadata)
                .build();
    }
}
