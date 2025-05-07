package com.fabiankevin.app.exceptions;

public class OtpAttemptLimitExceededException extends AppException {
    public OtpAttemptLimitExceededException() {
        super("OTP attempt limit exceeded:", 400);
    }
}
