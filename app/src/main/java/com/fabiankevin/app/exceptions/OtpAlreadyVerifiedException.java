package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class OtpAlreadyVerifiedException extends ApiException {
    public OtpAlreadyVerifiedException() {
        super("OTP has already been used", 400);
    }
}
