package com.fabiankevin.app.services;

import com.fabiankevin.app.exceptions.TotpAlreadyRegisteredException;
import com.fabiankevin.app.exceptions.TotpInvalidCodeException;
import com.fabiankevin.app.exceptions.TotpUnregisteredException;
import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.TotpUserRepository;
import com.fabiankevin.app.services.commands.GenerateQrCodeCommand;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;
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
//    private final OtpTransactionRepository otpTransactionRepository;

    @Override
    @Transactional
    public TotpUser registerTotp(RegisterTotpCommand command) {
        totpUserRepository.findByUserReferenceId(command.userReferenceId())
                .ifPresent(totp -> {
                    throw new TotpAlreadyRegisteredException();
                });

        String secret = secretGenerator.generate();
        Instant now = Instant.now();

        TotpUser totpUser = TotpUser.builder()
                .userReferenceId(command.userReferenceId())
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
                .algorithm("SHA1")
                .label(totpUser.userReferenceId())
                .secret(totpUser.secret())
                .issuer("App Label")
                .digits(6)
                .period(Duration.ofSeconds(30))
                .build());
    }

    @Override
    public void verifyTotp(String userReferenceId, String totpCode) {
        TotpUser totpUser = totpUserRepository.findByUserReferenceId(userReferenceId)
                .orElseThrow(TotpUnregisteredException::new);

        if (!totpCodeVerifier.verify(totpUser.secret(), totpCode)) {
            throw new TotpInvalidCodeException();
        }

        OffsetDateTime now = OffsetDateTime.now();

        OtpTransaction.builder()
                .recipient(totpUser.userReferenceId())
                .otpCode(totpCode)
                .purpose(null)
                .status(OtpStatus.VERIFIED)
                .deliveryMethod(DeliveryMethod.TOTP)
                .attemptCount(1)
                .updatedAt(now)
                .createdAt(now);

    }
}
