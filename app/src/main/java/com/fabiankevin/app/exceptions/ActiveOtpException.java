package com.fabiankevin.app.exceptions;

public class ActiveOtpException extends AppException {
    public ActiveOtpException(String userIdentifier){
        super("There is an active OTP for "+userIdentifier);
    }
}
