package com.fabiankevin.app.exceptions;

public class OtpNotFoundException extends AppException {
    public OtpNotFoundException() {
        super("Otp not found", 404);
    }
}
