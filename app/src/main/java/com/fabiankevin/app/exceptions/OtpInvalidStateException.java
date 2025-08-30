package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class OtpInvalidStateException extends ApiException {
    public OtpInvalidStateException() {
        super("Invalid OTP state", 400);
    }
}
