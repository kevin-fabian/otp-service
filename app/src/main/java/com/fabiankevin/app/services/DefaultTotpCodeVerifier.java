package com.fabiankevin.app.services;

import dev.samstevens.totp.code.CodeVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultTotpCodeVerifier implements TotpCodeVerifier {
    private final CodeVerifier codeVerifier;

    @Override
    public boolean verify(String secret, String totpCode) {
        boolean validSecret = Optional.ofNullable(secret)
                .filter(secretParam -> !secretParam.isBlank()).isPresent();
        boolean validTotpCode = Optional.ofNullable(totpCode)
                .filter(code -> !code.isBlank()).isPresent();

        if(!validSecret || !validTotpCode) {
            return false;
        }

        return codeVerifier.isValidCode(secret, totpCode);
    }
}
