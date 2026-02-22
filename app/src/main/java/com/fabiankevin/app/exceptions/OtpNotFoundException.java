package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;

public class OtpNotFoundException extends BusinessRuleException {
    public OtpNotFoundException() {
        super("Otp not found", 404, "AUTH_OTP_404");
    }
}
