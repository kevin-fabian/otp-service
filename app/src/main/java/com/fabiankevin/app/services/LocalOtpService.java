package com.fabiankevin.app.services;

import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.commands.VerifyOtpCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
@Slf4j
@RequiredArgsConstructor
public class LocalOtpService implements OtpService {
    private final OtpService otpService;

    @Override
    public Otp generate(GenerateOtpCommand command) {
        return otpService.generate(command);
    }

    @Override
    public void verify(VerifyOtpCommand command) {
        if ("123456".equals(command.otpCode())) {
            log.info("Local OTP verification bypassed.");
        }
    }
}
