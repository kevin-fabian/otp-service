package com.fabiankevin.app.services.totp;

import com.fabiankevin.app.exceptions.QrGeneratorException;
import com.fabiankevin.app.services.totp.commands.GenerateQrCodeCommand;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultQrGenerator implements QrGenerator {
    private final dev.samstevens.totp.qr.QrGenerator qrGenerator;
    private static final int DEFAULT_QR_PERIOD = 30; // Default period in seconds

    @Override
    public byte[] generate(GenerateQrCodeCommand command) {
        try {
            QrData qrData = new QrData.Builder()
                    .label(command.label())
                    .algorithm(HashingAlgorithm.valueOf(command.algorithm()))
                    .secret(command.secret())
                    .issuer(command.issuer())
                    .digits(command.digits())
                    .period(Optional.ofNullable(command.period())
                            .map(duration -> (int) duration.getSeconds())
                            .orElse(DEFAULT_QR_PERIOD))
                    .build();

            return qrGenerator.generate(qrData);
        } catch (QrGenerationException | IllegalArgumentException e) {
            log.error("Failed to generate QR code", e);
            throw new QrGeneratorException("Failed to generate QR code");
        }
    }
}
