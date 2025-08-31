package com.fabiankevin.app.services;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.commands.VerifyOtpCommand;

import java.util.UUID;

public interface OtpService {
    OtpTransaction generate(GenerateOtpCommand command);
    void verify(VerifyOtpCommand command);
    OtpTransaction retrieveById(UUID otpId);
    void markAsUsed(UUID otpId);
}
