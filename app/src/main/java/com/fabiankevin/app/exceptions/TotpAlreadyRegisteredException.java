package com.fabiankevin.app.exceptions;


import com.github.fabiankevin.lemon.web.exceptions.ApiException;

public class TotpAlreadyRegisteredException extends ApiException {
    public TotpAlreadyRegisteredException() {
        super("The user is already registered", 400);
    }
}
