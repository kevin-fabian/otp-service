package com.fabiankevin.app.exceptions;

public class OtpAttemptLimitExceededException extends ApiException {
    public OtpAttemptLimitExceededException() {
        super("OTP attempt limit exceeded", 249);
    }
}
