package com.fabiankevin.app.services.totp;

import com.fabiankevin.app.exceptions.TotpAlreadyRegisteredException;
import com.fabiankevin.app.exceptions.TotpInvalidCodeException;
import com.fabiankevin.app.exceptions.TotpUnregisteredException;
import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.persistence.TotpUserRepository;
import com.fabiankevin.app.properties.TotpProperties;
import com.fabiankevin.app.services.totp.commands.GenerateQrCodeCommand;
import com.fabiankevin.app.services.totp.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.totp.commands.VerifyTotpCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
@Service
public class DefaultTotpService implements TotpService {
    private final SecretGenerator secretGenerator;
    private final TotpUserRepository totpUserRepository;
    private final QrGenerator qrGenerator;
    private final TotpCodeVerifier totpCodeVerifier;
    private final TotpProperties properties;
    private final OtpTransactionRepository otpTransactionRepository;

    @Override
    @Transactional
    public TotpUser registerTotp(RegisterTotpCommand command) {
        totpUserRepository.findByUserReferenceId(command.userProfileId())
                .ifPresent(totp -> {
                    throw new TotpAlreadyRegisteredException();
                });

        String secret = secretGenerator.generate();
        Instant now = Instant.now();

        TotpUser totpUser = TotpUser.builder()
                .userReferenceId(command.userProfileId())
                .secret(secret)
                .updatedAt(now)
                .createdAt(now)
                .build();
        return totpUserRepository.save(totpUser);
    }

    @Override
    public byte[] getQrCodeImageByUserReferenceId(String userReferenceId) {
        TotpUser totpUser = totpUserRepository.findByUserReferenceId(userReferenceId)
                .orElseThrow(TotpUnregisteredException::new);

        return qrGenerator.generate(GenerateQrCodeCommand.builder()
                .algorithm(properties.getAlgorithm())
                .label(totpUser.userReferenceId())
                .secret(totpUser.secret())
                .issuer(properties.getIssuer())
                .digits(properties.getDigits())
                .period(Duration.ofSeconds(properties.getPeriodSeconds()))
                .build());
    }

    @Override
    @Transactional(noRollbackFor = TotpInvalidCodeException.class)
    public void verify(VerifyTotpCommand command) {
        String code = command.code();
        TotpUser totpUser = totpUserRepository.findById(command.id())
                .orElseThrow(TotpUnregisteredException::new);

        OffsetDateTime now = OffsetDateTime.now();

        OtpTransaction otpTransaction = otpTransactionRepository.retrieveRecipientAndActiveStatusAndNotExpired(totpUser.userReferenceId())
                .orElse(OtpTransaction.builder()
                        .recipient(totpUser.userReferenceId())
                        .otpCode(code)
                        .purpose(command.purpose())
                        .status(OtpStatus.ACTIVE)
                        .deliveryMethod(DeliveryMethod.TOTP)
                        .createdAt(now)
                        .updatedAt(now)
                        .expiresAt(now.plusSeconds(300))
                        .attemptCount(0)
                        .build());

        if (!totpCodeVerifier.verify(totpUser.secret(), code)) {
            otpTransactionRepository.save(otpTransaction.toBuilder()
                    .attemptCount(otpTransaction.attemptCount() + 1)
                    .status(OtpStatus.ACTIVE)
                    .updatedAt(now)
                    .build());
            throw new TotpInvalidCodeException();
        }

        otpTransactionRepository.save(otpTransaction.toBuilder()
                .status(OtpStatus.VERIFIED)
                .otpCode(code)
                .updatedAt(now)
                .build());
    }
}
