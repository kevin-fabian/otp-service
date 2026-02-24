package com.fabiankevin.app.services.otp;

import com.fabiankevin.app.clients.NotificationClient;
import com.fabiankevin.app.exceptions.*;
import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.otp.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.fabiankevin.app.models.enums.OtpStatus.NEW;
import static com.fabiankevin.app.models.enums.OtpStatus.SENT;

@RequiredArgsConstructor
@Slf4j
public class DefaultOneTimePasswordService implements OneTimePasswordService {
    private final OtpTransactionRepository otpTransactionRepository;
    private final Map<DeliveryMethod, NotificationClient> otpClientMap;
    private final OneTimePasswordGenerator oneTimePasswordGenerator;
    private final OtpProperties properties;

    @Override
    @Transactional
    public OneTimePasswordTransaction generate(GenerateOtpCommand command) {
        NotificationClient notificationClient = Optional.ofNullable(otpClientMap.get(command.deliveryMethod()))
                .orElseThrow(() -> new UnsupportedDeliveryMethodException(command.deliveryMethod()));
        return otpTransactionRepository.retrieveByRecipientAndStatus(command.recipient(), List.of(NEW, SENT))
                .map(otpTransaction -> {
                    if( otpTransaction.isNew() ){
                        sendOtp(notificationClient, otpTransaction);
                    }

                    return otpTransaction;
                })
                .orElseGet(() -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    String otpCode = oneTimePasswordGenerator.generateCode(properties.getCodeLength());
                    OneTimePasswordTransaction oneTimePasswordTransaction = OneTimePasswordTransaction.builder()
                            .deliveryMethod(command.deliveryMethod())
                            .purpose(command.purpose())
                            .recipient(command.recipient())
                            .metadata(command.metadata())
                            .status(NEW)
                            .otpCode(otpCode)
                            .attemptCount(0)
                            .createdAt(now.toInstant())
                            .updatedAt(now.toInstant())
                            .expiresAt(now.plusMinutes(properties.getExpirationMinutes()))
                            .build();

                    OneTimePasswordTransaction savedOneTimePasswordTransaction = otpTransactionRepository.saveAndFlush(oneTimePasswordTransaction);
                    sendOtp(notificationClient, savedOneTimePasswordTransaction);
                    return savedOneTimePasswordTransaction;
                });
    }

    private void sendOtp(NotificationClient notificationClient, OneTimePasswordTransaction oneTimePasswordTransaction) {
        notificationClient.sendAsync(oneTimePasswordTransaction)
                .thenRunAsync(() -> otpTransactionRepository.save(oneTimePasswordTransaction.toBuilder()
                        .updatedAt(Instant.now())
                        .status(SENT)
                        .build()));
    }

    @Override
    @Transactional(dontRollbackOn = {InvalidOtpException.class, OtpAttemptLimitExceededException.class, OtpExpiredException.class})
    public void verify(VerifyOtpCommand command) {
        final String code = command.otpCode();
        otpTransactionRepository.retrieveById(command.id())
                .ifPresentOrElse(otp -> {
                            if (!otp.isSent()) {
                                throw new OtpInvalidStateException();
                            }

                            int attempts = otp.attemptCount();
                            OffsetDateTime now = OffsetDateTime.now();
                            var otpBuilder = otp.toBuilder().updatedAt(now.toInstant());
                            if (otp.expiresAt().isBefore(OffsetDateTime.now())) {
                                throw new OtpExpiredException();
                            } else {
                                if (otp.otpCode().equalsIgnoreCase(code)) {
                                    otpBuilder.status(OtpStatus.VERIFIED).build();
                                    otpTransactionRepository.save(otpBuilder.build());
                                } else {
                                    attempts += 1;
                                    if (attempts >= properties.getMaxAttempts()) {
                                        otpTransactionRepository.save(otpBuilder.attemptCount(attempts).build());
                                        throw new OtpAttemptLimitExceededException();
                                    }

                                    otpTransactionRepository.save(otpBuilder.attemptCount(attempts).build());
                                    throw new InvalidOtpException(code);
                                }
                            }
                        },
                        () -> {
                            throw new OtpNotFoundException();
                        });
    }

    @Override
    public OneTimePasswordTransaction retrieveById(UUID otpId) {
        return otpTransactionRepository.retrieveById(otpId)
                .orElseThrow(OtpNotFoundException::new);
    }

    @Override
    public void useOtp(UUID otpId) {
        otpTransactionRepository.retrieveById(otpId)
                .ifPresentOrElse(otp -> {
                            if (otp.status() == OtpStatus.VERIFIED) {
                                otpTransactionRepository.save(otp.toBuilder()
                                        .status(OtpStatus.USED)
                                        .updatedAt(Instant.now())
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
