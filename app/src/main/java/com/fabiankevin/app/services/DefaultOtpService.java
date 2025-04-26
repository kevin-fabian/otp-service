package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.ActiveOtpException;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class DefaultOtpService implements OtpService {
    private final OtpRepository otpRepository;
    private final OtpClient client;
    private final OtpGenerator otpGenerator;

    private int otpCodeDigit = 3;
    private int maxAttemp = 3;
    private int expiresInMinutes = 5;

    @Override
    public Otp generate(GenerateOtpCommand command) {
        String userIdentifier = command.userIdentifier();

        if (otpRepository.existByUserIdentifierAndStatusActive(userIdentifier)) {
            throw new ActiveOtpException(userIdentifier);
        }
        LocalDateTime now = LocalDateTime.now();
        String otpCode = otpGenerator.generateCode(otpCodeDigit);
        Otp otp = Otp.builder()
                .deliveryMethod(command.deliveryMethod())
                .purpose(command.purpose())
                .userIdentifier(command.userIdentifier())
                .status(OtpStatus.ACTIVE)
                .otpCode(otpCode)
                .attemptCount(0)
                .createdAt(now)
                .expiresAt(now.plusMinutes(expiresInMinutes))
                .build();

        client.send(otp);

        return otpRepository.save(otp);
    }
}
