package com.fabiankevin.app.exceptions;

public class OtpExpiredException extends ApiException {
    public OtpExpiredException() {
        super("Otp is expired", 400);
    }
}
