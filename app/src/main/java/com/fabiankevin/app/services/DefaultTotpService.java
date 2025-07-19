package com.fabiankevin.app.services;

import com.fabiankevin.app.exceptions.TotpAlreadyRegisteredException;
import com.fabiankevin.app.exceptions.TotpInvalidCodeException;
import com.fabiankevin.app.exceptions.TotpUnregisteredException;
import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.persistence.TotpUserRepository;
import com.fabiankevin.app.services.commands.GenerateQrCodeCommand;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class DefaultTotpService implements TotpService {
    private final SecretGenerator secretGenerator;
    private final TotpUserRepository totpUserRepository;
    private final QrGenerator qrGenerator;
    private final TotpCodeVerifier totpCodeVerifier;

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
                .algorithm("SHA1")
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
    }
}
