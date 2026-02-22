package com.fabiankevin.app.exceptions;


import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;

public class OtpAttemptLimitExceededException extends BusinessRuleException {
    public OtpAttemptLimitExceededException() {
        super("OTP attempt limit exceeded", 429, "AUTH_OTP_003");
    }
}
