package com.fabiankevin.app.exceptions;

public class OtpAlreadyVerifiedException extends ApiException {
    public OtpAlreadyVerifiedException() {
        super("OTP has already been used", 400);
    }
}
