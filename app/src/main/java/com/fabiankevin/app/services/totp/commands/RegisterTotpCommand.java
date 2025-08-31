package com.fabiankevin.app.services.totp.commands;

import lombok.Builder;

@Builder(toBuilder = true)
public record RegisterTotpCommand(
        String userProfileId) {
}
