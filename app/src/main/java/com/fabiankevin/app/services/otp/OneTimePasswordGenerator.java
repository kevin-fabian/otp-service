package com.fabiankevin.app.services.otp;

public interface OneTimePasswordGenerator {
    String generateCode(int digit);
}
