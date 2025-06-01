package com.fabiankevin.app.exceptions;

public class OtpVerificationException extends ApiException {
    public OtpVerificationException(){
        super("Invalid Otp code", 400);
    }
}
