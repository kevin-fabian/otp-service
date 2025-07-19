package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class QrGeneratorException extends ApiException {
    public QrGeneratorException(String message, Throwable throwable) {
        super(message, 500, throwable);
    }
}
