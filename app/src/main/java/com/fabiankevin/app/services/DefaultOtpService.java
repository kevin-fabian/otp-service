package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.ActiveOtpException;
import com.fabiankevin.app.exceptions.UnsupportedDeliveryMethodException;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
public class DefaultOtpService implements OtpService {
    private final OtpRepository otpRepository;
    private final Map<DeliveryMethod, OtpClient> otpClientMap;
    private final OtpGenerator otpGenerator;
    private final OtpProperties properties;

    @Override
    public Otp generate(GenerateOtpCommand command) {
        String userIdentifier = command.userIdentifier();

        if (otpRepository.existByUserIdentifierAndStatusActive(userIdentifier)) {
            throw new ActiveOtpException(userIdentifier);
        }
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
    }
}
