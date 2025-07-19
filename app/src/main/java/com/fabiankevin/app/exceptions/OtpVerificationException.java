package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class OtpVerificationException extends ApiException {
    public OtpVerificationException(){
        super("Invalid Otp code", 400);
    }
}
