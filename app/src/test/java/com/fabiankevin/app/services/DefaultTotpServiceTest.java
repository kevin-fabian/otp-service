package com.fabiankevin.app.services;

import com.fabiankevin.app.exceptions.TotpAlreadyRegisteredException;
import com.fabiankevin.app.exceptions.TotpInvalidCodeException;
import com.fabiankevin.app.exceptions.TotpUnregisteredException;
import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.persistence.TotpUserRepository;
import com.fabiankevin.app.properties.TotpProperties;
import com.fabiankevin.app.services.totp.DefaultTotpService;
import com.fabiankevin.app.services.totp.QrGenerator;
import com.fabiankevin.app.services.totp.SecretGenerator;
import com.fabiankevin.app.services.totp.TotpCodeVerifier;
import com.fabiankevin.app.services.totp.commands.GenerateQrCodeCommand;
import com.fabiankevin.app.services.totp.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.totp.commands.VerifyTotpCommand;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultTotpServiceTest {
    private final SecretGenerator secretGenerator = mock(SecretGenerator.class);
    private final TotpUserRepository totpUserRepository = mock(TotpUserRepository.class);
    private final QrGenerator qrGenerator = mock(QrGenerator.class);
    private final TotpCodeVerifier totpCodeVerifier = mock(TotpCodeVerifier.class);
    private final TotpProperties properties = mock(TotpProperties.class);
    private final OtpTransactionRepository otpTransactionRepository = mock(OtpTransactionRepository.class);
    private final DefaultTotpService service = new DefaultTotpService(secretGenerator,
            totpUserRepository, qrGenerator, totpCodeVerifier, properties, otpTransactionRepository);

    @Test
    void registerTotp_givenNewUser_thenShouldRegisterSuccessfully() {
        String userReferenceId = "test-user";
        String generatedSecret = "test-secret";
        RegisterTotpCommand command = new RegisterTotpCommand(userReferenceId);

        when(secretGenerator.generate()).thenReturn(generatedSecret);
        when(totpUserRepository.findByUserReferenceId(userReferenceId)).thenReturn(Optional.empty());
        when(totpUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TotpUser result = service.registerTotp(command);

        assertNotNull(result, "Resulting TotpUser should not be null");
        assertEquals(userReferenceId, result.userReferenceId(), "User reference ID should be the same");
        assertEquals(generatedSecret, result.secret(), "Secret should match the generated secret");
        assertNotNull(result.createdAt(), "Created at timestamp should not be null");
        assertNotNull(result.updatedAt(), "Updated at timestamp should not be null");

        verify(totpUserRepository).save(argThat(totpUser ->
                userReferenceId.equalsIgnoreCase(totpUser.userReferenceId())));
    }

    @Test
    void registerTotp_givenAlreadyRegisteredUser_thenShouldThrowException() {
        String userReferenceId = "test-user";
        RegisterTotpCommand command = new RegisterTotpCommand(userReferenceId);

        when(totpUserRepository.findByUserReferenceId(userReferenceId)).thenReturn(Optional.of(TotpUser.builder()
                .userReferenceId(userReferenceId)
                .secret("existing-secret")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()));

        assertThrows(TotpAlreadyRegisteredException.class,
                () -> service.registerTotp(command),
                "Exception should be thrown when user is already registered"
        );

        verify(totpUserRepository, never()).save(any());
    }

    @Test
    void getQrCodeImageByUserReferenceId_givenExistingUser_thenShouldReturnQrCodeImage() {
        when(properties.getAlgorithm()).thenReturn("SHA1");
        when(properties.getIssuer()).thenReturn("App Label");
        when(properties.getDigits()).thenReturn(6);
        when(properties.getPeriodSeconds()).thenReturn(30);

        String userReferenceId = "test-user";
        String secret = "test-secret";
        byte[] qrCodeImage = new byte[]{1, 2, 3};

        TotpUser totpUser = TotpUser.builder()
                .userReferenceId(userReferenceId)
                .secret(secret)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        GenerateQrCodeCommand expectedCommand = GenerateQrCodeCommand.builder()
                .algorithm("SHA1")
                .label(userReferenceId)
                .secret(secret)
                .issuer("App Label")
                .digits(6)
                .period(Duration.ofSeconds(30))
                .build();

        when(totpUserRepository.findByUserReferenceId(userReferenceId)).thenReturn(Optional.of(totpUser));
        when(qrGenerator.generate(expectedCommand)).thenReturn(qrCodeImage);

        byte[] result = service.getQrCodeImageByUserReferenceId(userReferenceId);

        assertArrayEquals(qrCodeImage, result, "QR code image should match the generated image");
        verify(totpUserRepository).findByUserReferenceId(userReferenceId);
        ArgumentCaptor<GenerateQrCodeCommand> commandCaptor = ArgumentCaptor.forClass(GenerateQrCodeCommand.class);
        verify(qrGenerator).generate(commandCaptor.capture());
        GenerateQrCodeCommand capturedCommand = commandCaptor.getValue();
        assertEquals(expectedCommand, capturedCommand, "Command should match");
    }

    @Test
    void getQrCodeImageByUserReferenceId_givenNonExistentUser_thenShouldThrowException() {
        String userReferenceId = "test-user";

        when(totpUserRepository.findByUserReferenceId(userReferenceId)).thenReturn(Optional.empty());

        assertThrows(TotpUnregisteredException.class,
                () -> service.getQrCodeImageByUserReferenceId(userReferenceId),
                "Exception should be thrown when user is not found"
        );

        verify(totpUserRepository).findByUserReferenceId(userReferenceId);
        verifyNoInteractions(qrGenerator);
    }

    @Test
    void verify_givenValidCode_thenShouldVerifySuccessfully() {
        String userReferenceId = "test-user";
        String totpCode = "123456";
        String secret = "test-secret";

        TotpUser totpUser = TotpUser.builder()
                .userReferenceId(userReferenceId)
                .secret(secret)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(totpUserRepository.findById(any())).thenReturn(Optional.of(totpUser));
        when(totpCodeVerifier.verify(secret, totpCode)).thenReturn(true);

        assertDoesNotThrow(() -> service.verify(VerifyTotpCommand.builder()
                        .userReferenceId(userReferenceId)
                        .code(totpCode)
                        .purpose(OtpPurpose.TRANSACTION)
                        .build()),
                "Should not throw exception when TOTP code is valid");

        verify(totpUserRepository).findById(any());
        verify(totpCodeVerifier).verify(secret, totpCode);
    }

    @Test
    void verify_givenInvalidCode_thenShouldThrowException() {
        String userReferenceId = "test-user";
        String totpCode = "123456";
        String secret = "test-secret";

        TotpUser totpUser = TotpUser.builder()
                .id(UUID.randomUUID())
                .userReferenceId(userReferenceId)
                .secret(secret)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(totpUserRepository.findById(totpUser.id())).thenReturn(Optional.of(totpUser));
        when(totpCodeVerifier.verify(secret, totpCode)).thenReturn(false);

        VerifyTotpCommand verifyCommand = VerifyTotpCommand.builder()
                .userReferenceId(userReferenceId)
                .code(totpCode)
                .purpose(OtpPurpose.TRANSACTION)
                .build();;

        assertThrows(TotpInvalidCodeException.class,
                () -> service.verify(verifyCommand),
                "Exception should be thrown when TOTP code is invalid");

        verify(totpUserRepository).findById(totpUser.id());
        verify(totpCodeVerifier).verify(secret, totpCode);
    }

    @Test
    void verify_givenNonExistentUser_thenShouldThrowException() {
        String userReferenceId = "test-user";
        String totpCode = "123456";

        when(totpUserRepository.findById(any())).thenReturn(Optional.empty());

        VerifyTotpCommand verifyCommand = VerifyTotpCommand.builder()
                .userReferenceId(userReferenceId)
                .code(totpCode)
                .purpose(OtpPurpose.TRANSACTION)
                .build();;

        assertThrows(TotpUnregisteredException.class,
                () -> service.verify(verifyCommand),
                "Exception should be thrown when the user is not found");

        verify(totpUserRepository).findById(any());
        verifyNoInteractions(totpCodeVerifier);
    }
}