package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.OtpNotFoundException;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.commands.VerifyOtpCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class LocalOtpService extends DefaultOtpService {
    private final OtpTransactionRepository otpTransactionRepository;

    public LocalOtpService(OtpTransactionRepository otpTransactionRepository, Map<DeliveryMethod, OtpClient> otpClientMap, OtpGenerator otpGenerator, OtpProperties properties) {
        super(otpTransactionRepository, otpClientMap, otpGenerator, properties);
        this.otpTransactionRepository = otpTransactionRepository;
    }

    @Override
    public void verify(VerifyOtpCommand command) {
        otpTransactionRepository.retrieveById(command.id())
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
