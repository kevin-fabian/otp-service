package com.fabiankevin.app.services.otp;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.services.otp.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;

import java.util.UUID;

public interface OneTimePasswordService {
    OneTimePasswordTransaction generate(GenerateOtpCommand command);
    void verify(VerifyOtpCommand command);
    OneTimePasswordTransaction retrieveById(UUID otpId);
    void useOtp(UUID otpId);
}
