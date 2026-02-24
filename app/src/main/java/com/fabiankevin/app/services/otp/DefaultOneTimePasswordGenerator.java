package com.fabiankevin.app.services.otp;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DefaultOneTimePasswordGenerator implements OneTimePasswordGenerator {
    private final Random random = new Random();
    @Override
    public String generateCode(int digits) {
        if (digits < 1) {
            throw new IllegalArgumentException("Number of digits must be at least 1");
        }

        int min = (int) Math.pow(10, digits - 1); // e.g., 10000 for 5 digits
        int max = (int) Math.pow(10, digits) - 1; // e.g., 99999 for 5 digits
        return String.valueOf(min + random.nextInt(max - min + 1));
    }
}
