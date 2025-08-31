package com.fabiankevin.app.services;

import com.fabiankevin.app.services.totp.DefaultTotpCodeVerifier;
import com.fabiankevin.app.services.totp.TotpCodeVerifier;
import dev.samstevens.totp.code.CodeVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DefaultTotpCodeVerifierTest {
    private final CodeVerifier codeVerifier = mock(CodeVerifier.class);
    private final TotpCodeVerifier totpCodeVerifier = new DefaultTotpCodeVerifier(codeVerifier);

    @Test
    void verify_givenValidSecretAndCode_thenShouldReturnTrue() {
        String secret = "validSecret";
        String totpCode = "123456";

        when(codeVerifier.isValidCode(secret, totpCode)).thenReturn(true);

        boolean valid = totpCodeVerifier.verify(secret, totpCode);

        verify(codeVerifier, times(1)).isValidCode(secret, totpCode);
        assertTrue(valid, "The verification should succeed with valid secret and code");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void verify_givenNullSecret_thenShouldReturnFalse(String invalidSecret) {
        boolean isValid = totpCodeVerifier.verify(invalidSecret, "123456");

        assertFalse(isValid, "The verification should fail when the secret is null");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void verify_givenNullTotpCode_thenShouldReturnFalse(String invalidCode) {
        boolean isValid = totpCodeVerifier.verify("validSecret", invalidCode);

        verifyNoInteractions(codeVerifier);
        assertFalse(isValid, "The verification should fail when the TOTP code is null");
    }
}