package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.*;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.commands.VerifyOtpCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class DefaultOtpService implements OtpService {
    private final OtpRepository otpRepository;
    private final Map<DeliveryMethod, OtpClient> otpClientMap;
    private final OtpGenerator otpGenerator;
    private final OtpProperties properties;

    @Override
    @Transactional
    public Otp generate(GenerateOtpCommand command) {
        return otpRepository.retrieveByUserIdentifierAndActiveStatusAndNotExpired(command.userIdentifier())
                .orElseGet(() -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    String otpCode = otpGenerator.generateCode(properties.getCodeLength());
                    Otp otp = Otp.builder()
                            .deliveryMethod(command.deliveryMethod())
                            .purpose(command.purpose())
                            .userIdentifier(command.userIdentifier())
                            .metadata(command.metadata())
                            .status(OtpStatus.ACTIVE)
                            .otpCode(otpCode)
                            .attemptCount(0)
                            .createdAt(now)
                            .expiresAt(now.plusMinutes(properties.getExpirationMinutes()))
                            .build();

                    Otp savedOtp = otpRepository.saveAndFlush(otp);

                    Optional.ofNullable(otpClientMap.get(otp.deliveryMethod()))
                            .orElseThrow(() -> new UnsupportedDeliveryMethodException(otp.deliveryMethod()))
                            .send(savedOtp);

                    return savedOtp;
                });
    }

    @Override
    @Transactional(dontRollbackOn = {OtpVerificationException.class, OtpAttemptLimitExceededException.class, OtpExpiredException.class})
    public void verify(VerifyOtpCommand command) {
        Otp savedOtp = otpRepository.retrieveById(command.id())
                .map(otp -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    var otpBuilder = otp.toBuilder().updatedAt(now);
                    if (otp.expiresAt().isBefore(OffsetDateTime.now())) {
                        otpBuilder.status(OtpStatus.EXPIRED);
                    }

                    if (otp.otpCode().equalsIgnoreCase(command.otpCode())) {
                        otpBuilder.status(OtpStatus.USED).build();
                    }

                    int attempts = otp.attemptCount() + 1;
                    if (attempts >= properties.getMaxAttempts()) {
                        otpBuilder.status(OtpStatus.INVALIDATED);
                    }

                    return otpRepository.save(otpBuilder.attemptCount(attempts).build());
                })
                .orElseThrow(OtpNotFoundException::new);

        switch (savedOtp.status()) {
            case ACTIVE -> throw new OtpVerificationException();
            case EXPIRED -> throw new OtpExpiredException();
            case INVALIDATED -> throw new OtpAttemptLimitExceededException();
            default -> log.info("Otp has been verified.");
        }
    }
}
