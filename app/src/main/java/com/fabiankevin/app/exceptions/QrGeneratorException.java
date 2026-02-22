package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.lemon.web.exceptions.ApiException;

public class QrGeneratorException extends ApiException {
    public QrGeneratorException(String message) {
        super(message, 500);
    }
}
