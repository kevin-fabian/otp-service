package com.fabiankevin.app.services.otp;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.services.otp.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;

import java.util.UUID;

public interface OtpService {
    OtpTransaction generate(GenerateOtpCommand command);
    void verify(VerifyOtpCommand command);
    OtpTransaction retrieveById(UUID otpId);
    void useOtp(UUID otpId);
}
