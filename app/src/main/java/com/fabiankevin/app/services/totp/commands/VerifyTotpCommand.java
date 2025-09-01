package com.fabiankevin.app.services.totp.commands;

import com.fabiankevin.app.models.enums.OtpPurpose;
import lombok.Builder;

@Builder
public record VerifyTotpCommand(String userReferenceId, String code, OtpPurpose purpose) {
}
