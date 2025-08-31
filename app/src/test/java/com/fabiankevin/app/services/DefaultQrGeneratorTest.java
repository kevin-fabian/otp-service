package com.fabiankevin.app.services;

import com.fabiankevin.app.exceptions.QrGeneratorException;
import com.fabiankevin.app.services.totp.DefaultQrGenerator;
import com.fabiankevin.app.services.totp.commands.GenerateQrCodeCommand;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultQrGeneratorTest {
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final DefaultQrGenerator defaultQrGenerator = new DefaultQrGenerator(qrGenerator);

    @Test
    void generate_givenValidCommand_thenShouldGenerateQrCode() {
        GenerateQrCodeCommand command = new GenerateQrCodeCommand(
                "test-label",
                HashingAlgorithm.SHA1.name(),
                "test-secret",
                "test-issuer",
                6,
                Duration.ofSeconds(30)
        );

        byte[] result = defaultQrGenerator.generate(command);
        assertTrue(result.length > 0, "Generated QR code should not be empty");
    }

    @Test
    void generate_givenInvalidAlgorithm_thenShouldThrowException() {
        GenerateQrCodeCommand command = new GenerateQrCodeCommand(
                "test-label",
                "INVALID_ALGORITHM",
                "test-secret",
                "test-issuer",
                6,
                Duration.ofSeconds(30)
        );

        assertThrows(QrGeneratorException.class,
                () -> defaultQrGenerator.generate(command),
                "Should throw QrGeneratorException for invalid algorithm");
    }
}