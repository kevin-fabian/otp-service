package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class OtpAttemptLimitExceededException extends ApiException {
    public OtpAttemptLimitExceededException() {
        super("OTP attempt limit exceeded", 429);
    }
}
