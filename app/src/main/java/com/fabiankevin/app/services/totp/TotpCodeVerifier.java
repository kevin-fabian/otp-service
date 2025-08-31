package com.fabiankevin.app.services.totp;

public interface TotpCodeVerifier {
    boolean verify(String secret, String totpCode);
}
