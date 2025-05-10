package com.fabiankevin.app.exceptions;

public class OtpAlreadyVerifiedException extends AppException {
    public OtpAlreadyVerifiedException() {
        super("OTP has already been used", 400);
    }
}
