package com.fabiankevin.app.services;

import com.fabiankevin.app.services.totp.SecretGenerator;
import com.fabiankevin.app.services.totp.TotpSecretGenerator;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpUserSecretGeneratorTest {
    private final SecretGenerator secretGenerator = new TotpSecretGenerator();

    @Test
    void generate_givenValidRequest_thenShouldGenerateBase32EncodedSecret() {
        String secret = secretGenerator.generate();

        assertTrue(new Base32().isInAlphabet(secret),
                "Generated secret should be correctly Base32 encoded");
    }

    @Test
    void generate_givenMultipleInvocations_thenShouldGenerateUniqueSecrets() {
        Set<String> secrets = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            secrets.add(secretGenerator.generate());
        }

        assertEquals(100, secrets.size(), "Generated secrets should be unique across multiple invocations");
    }
}