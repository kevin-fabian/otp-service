package com.fabiankevin.app.services;

import dev.samstevens.totp.code.CodeVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DefaultTotpCodeVerifier implements TotpCodeVerifier {
    private final CodeVerifier codeVerifier;

    @Override
    public boolean verify(String secret, String totpCode) {
        return codeVerifier.isValidCode(secret, totpCode);
    }
}
