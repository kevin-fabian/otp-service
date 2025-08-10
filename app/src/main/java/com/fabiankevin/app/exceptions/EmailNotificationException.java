package com.fabiankevin.app.exceptions;

import com.github.fabiankevin.microwebspringbootstarter.exceptions.ApiException;

public class EmailNotificationException extends ApiException {
    public EmailNotificationException(String recipient, Throwable cause){
        super("Unable to send an email to "+recipient, 500, cause);
    }
}
