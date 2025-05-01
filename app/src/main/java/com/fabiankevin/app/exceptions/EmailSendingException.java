package com.fabiankevin.app.exceptions;

public class EmailSendingException extends AppException {
    public EmailSendingException(String userIdentifier, Throwable cause){
        super("Unable to send an email to "+userIdentifier, 500, cause);
    }
}
