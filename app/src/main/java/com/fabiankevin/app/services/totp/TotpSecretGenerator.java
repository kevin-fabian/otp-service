package com.fabiankevin.app.services.totp;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TotpSecretGenerator implements SecretGenerator {
    private static final int DEFAULT_TOTP_SECRET_LENGTH = 20;
    private final Base32 base32 = new Base32();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        return base32.encodeToString(secureRandom.generateSeed(DEFAULT_TOTP_SECRET_LENGTH));
    }
}
