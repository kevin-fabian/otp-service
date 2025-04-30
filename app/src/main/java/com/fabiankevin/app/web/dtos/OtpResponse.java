package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.Otp;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record OtpResponse(
        UUID id,
        String otpCode,
        OffsetDateTime expiredAt) {

    public static OtpResponse from(Otp otp) {
        return OtpResponse.builder()
                .id(otp.id())
                .otpCode(otp.otpCode())
                .expiredAt(otp.expiresAt())
                .build();
    }
}