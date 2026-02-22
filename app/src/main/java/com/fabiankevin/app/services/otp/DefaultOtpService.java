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
public class DefaultOtpService implements OtpService {
    private final OtpTransactionRepository otpTransactionRepository;
    private final Map<DeliveryMethod, OtpClient> otpClientMap;
    private final OtpGenerator otpGenerator;
    private final OtpProperties properties;

    @Override
    @Transactional
    public OtpTransaction generate(GenerateOtpCommand command) {
        return otpTransactionRepository.retrieveByRecipientAndStatus(command.recipient(), List.of(NEW, SENT))
                .map(otpTransaction -> {
                    if( otpTransaction.isNew() ){
                        sendOtp(otpTransaction);
                    }

                    return otpTransaction;
                })
                .orElseGet(() -> {
                    OffsetDateTime now = OffsetDateTime.now();
                    String otpCode = otpGenerator.generateCode(properties.getCodeLength());
                    OtpTransaction otpTransaction = OtpTransaction.builder()
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

                    OtpTransaction savedOtpTransaction = otpTransactionRepository.save(otpTransaction);
                    sendOtp(savedOtpTransaction);
                    return savedOtpTransaction;
                });
    }

    private void sendOtp(OtpTransaction otpTransaction) {
        OtpClient otpClient = Optional.ofNullable(otpClientMap.get(otpTransaction.deliveryMethod()))
                .orElseThrow(() -> new UnsupportedDeliveryMethodException(otpTransaction.deliveryMethod()));
        otpClient.sendAsync(otpTransaction)
                .thenAcceptAsync(_ -> otpTransactionRepository.saveAndFlush(otpTransaction.toBuilder()
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
    public OtpTransaction retrieveById(UUID otpId) {
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
