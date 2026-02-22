package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;

public class OtpInvalidStateException extends BusinessRuleException {
    public OtpInvalidStateException() {
        super("Invalid OTP state", 400, "AUTH_OTP_004");
    }
}
