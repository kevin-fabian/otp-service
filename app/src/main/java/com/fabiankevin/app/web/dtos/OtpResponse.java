package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.Otp;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record OtpResponse(
        UUID id,
        @JsonProperty("otp_code")
        @Schema(description = "Otp code could be 4-6 digits depends on configuration",
                example = "123456")
        String otpCode,
        @Schema(description = "Date and time when OTP will expire", example = "2024-03-01T12:00:00.000+00:00")
        OffsetDateTime expiredAt) {

    public static OtpResponse from(Otp otp) {
        return OtpResponse.builder()
                .id(otp.id())
                .otpCode(otp.otpCode())
                .expiredAt(otp.expiresAt())
                .build();
    }
}