package com.fabiankevin.app.services.commands;

import com.fabiankevin.app.models.enums.OtpPurpose;
import lombok.Builder;

import java.util.UUID;

@Builder
public record VerifyTotpCommand(UUID id, String code, OtpPurpose purpose) {
}
