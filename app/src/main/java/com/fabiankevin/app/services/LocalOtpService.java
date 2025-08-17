package com.fabiankevin.app.services;

import com.fabiankevin.app.exceptions.OtpNotFoundException;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.commands.VerifyOtpCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LocalOtpService implements OtpService {
    private final OtpService otpService;
    private final OtpRepository otpRepository;

    @Override
    public Otp generate(GenerateOtpCommand command) {
        return otpService.generate(command);
    }

    @Override
    public void verify(VerifyOtpCommand command) {
        otpRepository.retrieveById(command.id())
                .ifPresentOrElse(
                        otp -> {
                            if ("123456".equals(command.otpCode())) {
                                log.info("Local OTP verification bypassed.");
                            }
                        },
                        () -> {
                            throw new OtpNotFoundException();
                        }
                );

    }
}
