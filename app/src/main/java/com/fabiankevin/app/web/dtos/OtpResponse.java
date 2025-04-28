package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.Otp;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder(toBuilder = true)
public record OtpResponse(
        UUID id,
        String otpCode,
        LocalDateTime createdAt) {

    public static OtpResponse from(Otp otp) {
        return OtpResponse.builder()
                .id(otp.id())
                .otpCode(otp.otpCode())
                .createdAt(otp.createdAt())
                .build();
    }
}