package com.fabiankevin.app.services.otp.commands;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record VerifyOtpCommand(UUID id, String otpCode) {
    
}