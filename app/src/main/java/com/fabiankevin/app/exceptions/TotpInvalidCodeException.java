package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.lemon.web.exceptions.BusinessRuleException;

public class TotpInvalidCodeException extends BusinessRuleException {
    public TotpInvalidCodeException() {
        super("Invalid TOTP code. Please try again.", 400, "AUTH_TOTP_001");
    }
}
