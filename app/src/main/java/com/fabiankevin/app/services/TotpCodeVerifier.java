package com.fabiankevin.app.services;

public interface TotpCodeVerifier {
    boolean verify(String secret, String totpCode);
}
