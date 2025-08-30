package com.fabiankevin.app.exceptions;

public class OtpInvalidStateException extends RuntimeException {
    public OtpInvalidStateException() {
        super("Invalid OTP state");
    }
}
