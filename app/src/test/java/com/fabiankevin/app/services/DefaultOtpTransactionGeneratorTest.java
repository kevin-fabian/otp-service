package com.fabiankevin.app.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultOtpTransactionGeneratorTest {

    private final OtpGenerator generator = new DefaultOtpGenerator();

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4, 6})
    void generateCode_givenValidDigits_thenShouldReturnCodeOfCorrectLength(int digits) {
        String code = generator.generateCode(digits);

        assertEquals(digits, code.length(), "Generated OTP should have the correct number of digits");
    }

    @Test
    void generateCode_givenInvalidDigits_thenShouldThrowException() {
        int invalidDigits = 0;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> generator.generateCode(invalidDigits),
                "Expected exception when digits are zero or less");
        assertEquals("Number of digits must be at least 1", exception.getMessage(), "Exception message should match");
    }
}
