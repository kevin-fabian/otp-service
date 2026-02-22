package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;

public class InvalidOtpException extends BusinessRuleException {
    public InvalidOtpException(String code) {
        super("Invalid OTP code: " + code, 400, "AUTH_OTP_001");
    }
}
