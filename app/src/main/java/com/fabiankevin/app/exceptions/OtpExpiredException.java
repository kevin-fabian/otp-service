package com.fabiankevin.app.exceptions;


import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;

public class OtpExpiredException extends BusinessRuleException {
    public OtpExpiredException() {
        super("Otp is expired", 400, "AUTH_OTP_002");
    }
}
