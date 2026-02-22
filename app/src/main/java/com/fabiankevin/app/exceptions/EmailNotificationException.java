package com.fabiankevin.app.exceptions;


import com.github.fabiankevin.lemon.web.exceptions.ApiException;

public class EmailNotificationException extends ApiException {
    public EmailNotificationException(String recipient){
        super("Unable to send an email to "+recipient, 500);
    }
}
