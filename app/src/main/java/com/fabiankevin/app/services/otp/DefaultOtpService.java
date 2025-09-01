package com.fabiankevin.app.services.otp;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.*;
import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.otp.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class DefaultOtpService implements OtpService {
    private final OtpTransactionRepository otpTransactionRepository;
    private final Map<DeliveryMethod, OtpClient> otpClientMap;
    private final OtpGenerator otpGenerator;
    private final OtpProperties properties;

    @Override
    @Transactional
    public OtpTransaction generate(GenerateOtpCommand command) {
        return otpTransactionRepository.retrieveByRecipientAndActiveStatusAndNotExpired(command.recipient())
                .orElseGet(() -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    String otpCode = otpGenerator.generateCode(properties.getCodeLength());
                    OtpTransaction otpTransaction = OtpTransaction.builder()
                            .deliveryMethod(command.deliveryMethod())
                            .purpose(command.purpose())
                            .recipient(command.recipient())
                            .metadata(command.metadata())
                            .status(OtpStatus.ACTIVE)
                            .otpCode(otpCode)
                            .attemptCount(0)
                            .createdAt(now)
                            .updatedAt(now)
                            .expiresAt(now.plusMinutes(properties.getExpirationMinutes()))
                            .build();

                    OtpTransaction savedOtpTransaction = otpTransactionRepository.saveAndFlush(otpTransaction);

                    Optional.ofNullable(otpClientMap.get(otpTransaction.deliveryMethod()))
                            .orElseThrow(() -> new UnsupportedDeliveryMethodException(otpTransaction.deliveryMethod()))
                            .send(savedOtpTransaction);

                    return savedOtpTransaction;
                });
    }

    @Override
    @Transactional(dontRollbackOn = {OtpVerificationException.class, OtpAttemptLimitExceededException.class, OtpExpiredException.class})
    public void verify(VerifyOtpCommand command) {
        OtpTransaction savedOtpTransaction = otpTransactionRepository.retrieveById(command.id())
                .map(otp -> {
                    if (!otp.isActive()) {
                        throw new OtpInvalidStateException();
                    }

                    int attempts = otp.attemptCount();

                    OffsetDateTime now = OffsetDateTime.now();
                    var otpBuilder = otp.toBuilder().updatedAt(now);
                    if (otp.expiresAt().isBefore(OffsetDateTime.now())) {
                        otpBuilder.status(OtpStatus.EXPIRED);
                    } else {
                        if (otp.otpCode().equalsIgnoreCase(command.otpCode())) {
                            otpBuilder.status(OtpStatus.VERIFIED).build();
                        }

                        attempts = otp.attemptCount() + 1;
                        if (attempts >= properties.getMaxAttempts()) {
                            otpBuilder.status(OtpStatus.INVALIDATED);
                        }
                    }

                    return otpTransactionRepository.save(otpBuilder.attemptCount(attempts).build());
                })
                .orElseThrow(OtpNotFoundException::new);

        switch (savedOtpTransaction.status()) {
            case ACTIVE -> throw new OtpVerificationException();
            case EXPIRED -> throw new OtpExpiredException();
            case INVALIDATED -> throw new OtpAttemptLimitExceededException();
            default -> log.info("Otp has been verified.");
        }
    }

    @Override
    public OtpTransaction retrieveById(UUID otpId) {
        return otpTransactionRepository.retrieveById(otpId)
                .orElseThrow(OtpNotFoundException::new);
    }

    @Override
    public void markAsUsed(UUID otpId) {
        otpTransactionRepository.retrieveById(otpId)
                .ifPresentOrElse(otp -> {
                            if (otp.status() == OtpStatus.VERIFIED) {
                                otpTransactionRepository.save(otp.toBuilder()
                                        .status(OtpStatus.USED)
                                        .updatedAt(OffsetDateTime.now())
                                        .build());
                            } else {
                                throw new OtpInvalidStateException();
                            }
                        },
                        () -> {
                            throw new OtpNotFoundException();
                        });
    }
}
