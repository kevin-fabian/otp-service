package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class OtpExpiredException extends ApiException {
    public OtpExpiredException() {
        super("Otp is expired", 400);
    }
}
