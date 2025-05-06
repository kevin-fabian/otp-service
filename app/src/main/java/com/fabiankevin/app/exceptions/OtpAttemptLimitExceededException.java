package com.fabiankevin.app.exceptions;

public class OtpAttemptLimitExceededException extends AppException {
    public OtpAttemptLimitExceededException(String attempt) {
        super("OTP attempt limit exceeded: %s".formatted(attempt), 400);
    }
}
