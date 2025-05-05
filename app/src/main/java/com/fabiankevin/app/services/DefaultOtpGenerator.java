package com.fabiankevin.app.services;

import java.util.Random;

public class DefaultOtpGenerator implements OtpGenerator {
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
