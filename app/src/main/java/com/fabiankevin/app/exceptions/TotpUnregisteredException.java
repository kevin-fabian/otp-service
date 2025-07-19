package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class TotpUnregisteredException extends ApiException {
    public TotpUnregisteredException() {
        super("Unregistered", 404);
    }
}
