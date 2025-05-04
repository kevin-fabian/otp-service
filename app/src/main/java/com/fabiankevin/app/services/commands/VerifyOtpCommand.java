package com.fabiankevin.app.services.commands;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record VerifyOtpCommand(UUID id, String userIdentifier, String otpCode) {
    
}