package com.fabiankevin.app.services;

import org.springframework.stereotype.Component;

@Component
public class JasyptSecretEncryptor implements SecretEncryptor {
    @Override
    public String encrypt(String secret) {
        return secret;
    }
}
