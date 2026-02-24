package com.fabiankevin.app.services.otp;

import com.fabiankevin.app.clients.NotificationClient;
import com.fabiankevin.app.exceptions.InvalidOtpException;
import com.fabiankevin.app.exceptions.OtpNotFoundException;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class AutotestOneTimePasswordService extends DefaultOneTimePasswordService {
    private final OtpTransactionRepository otpTransactionRepository;

    public AutotestOneTimePasswordService(OtpTransactionRepository otpTransactionRepository, Map<DeliveryMethod, NotificationClient> otpClientMap, OneTimePasswordGenerator oneTimePasswordGenerator, OtpProperties properties) {
        super(otpTransactionRepository, otpClientMap, oneTimePasswordGenerator, properties);
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
                            else {
                                throw new InvalidOtpException(command.otpCode());
                            }
                        },
                        () -> {
                            throw new OtpNotFoundException();
                        }
                );

    }
}
