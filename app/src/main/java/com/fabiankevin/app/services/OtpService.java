package com.fabiankevin.app.services;

import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;

public interface OtpService {
    Otp generate(GenerateOtpCommand command);
}
