package com.fabiankevin.app.exceptions;

public class OtpExpiredException extends AppException {
    public OtpExpiredException() {
        super("Otp is expired", 400);
    }
}
