package com.fabiankevin.app.exceptions;

public class OtpVerificationException extends AppException {
    public OtpVerificationException(){
        super("Invalid Otp code", 400);
    }
}
