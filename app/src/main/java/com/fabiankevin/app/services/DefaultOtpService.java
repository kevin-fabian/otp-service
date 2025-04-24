package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.ActiveOtpException;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

@RequiredArgsConstructor
public class DefaultOtpService implements OtpService {
    private final OtpRepository otpRepository;
    private final OtpClient client;

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
        String otpCode = String.valueOf(generateRandomNumber(otpCodeDigit));
        Otp otp = Otp.builder()
                .userIdentifier(userIdentifier)
                .attemptCount(0)
                .deliveryMethod(command.deliveryMethod())
                .deliveryTarget(command.deliveryTarget())
                .createdAt(now)
                .expiresAt(now.plusMinutes(expiresInMinutes))
                .purpose(command.purpose())
                .otpCode(otpCode)
                .metadata(command.metadata())
                .build();

        return otpRepository.save(otp);
    }

    private static int generateRandomNumber(int digits) {
        if (digits < 1) {
            throw new IllegalArgumentException("Number of digits must be at least 1");
        }

        Random random = new Random();
        int min = (int) Math.pow(10, digits - 1); // e.g., 10000 for 5 digits
        int max = (int) Math.pow(10, digits) - 1; // e.g., 99999 for 5 digits
        return min + random.nextInt(max - min + 1);
    }
}
