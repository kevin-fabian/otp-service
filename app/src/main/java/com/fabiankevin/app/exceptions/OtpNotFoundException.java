package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class OtpNotFoundException extends ApiException {
    public OtpNotFoundException() {
        super("Otp not found", 404);
    }
}
