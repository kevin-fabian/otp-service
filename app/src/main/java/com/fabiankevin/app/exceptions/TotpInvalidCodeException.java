package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class TotpInvalidCodeException extends ApiException {
    public TotpInvalidCodeException() {
        super("Invalid TOTP code. Please try again.", 400);
    }
}
