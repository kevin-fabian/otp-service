package com.fabiankevin.app.services;

import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.commands.VerifyOtpCommand;

import java.util.UUID;

public interface OtpService {
    Otp generate(GenerateOtpCommand command);
    void verify(VerifyOtpCommand command);
    Otp retrieveById(UUID otpId);
    void markAsUsed(UUID otpId);
}
