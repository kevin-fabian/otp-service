package com.fabiankevin.app.exceptions;

public class AppException extends RuntimeException {
    private int httpCode;

    public AppException(String message) {
        super(message);
    }

    public AppException(int httpCode, String message) {
        super(message);
        this.httpCode = httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
