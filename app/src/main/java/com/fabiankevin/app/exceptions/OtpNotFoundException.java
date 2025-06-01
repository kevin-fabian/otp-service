package com.fabiankevin.app.exceptions;

public class OtpNotFoundException extends ApiException {
    public OtpNotFoundException() {
        super("Otp not found", 404);
    }
}
