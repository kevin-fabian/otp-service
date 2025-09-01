package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.*;
import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.otp.DefaultOtpService;
import com.fabiankevin.app.services.otp.OtpGenerator;
import com.fabiankevin.app.services.otp.OtpService;
import com.fabiankevin.app.services.otp.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class DefaultOtpTransactionServiceTest {
    private final OtpTransactionRepository otpTransactionRepository = mock(OtpTransactionRepository.class);
    private final OtpClient smsOtpClient = mock(OtpClient.class);
    private final Map<DeliveryMethod, OtpClient> otpClientMap = spy(
            Map.of(DeliveryMethod.SMS, smsOtpClient)
    );
    private final OtpGenerator otpGenerator = mock(OtpGenerator.class);
    private final OtpProperties otpProperties = mock(OtpProperties.class);
    private final OtpService otpService = new DefaultOtpService(otpTransactionRepository, otpClientMap, otpGenerator, otpProperties);

    private GenerateOtpCommand mockedCommand;

    @BeforeEach
    void setup() {
        mockedCommand = GenerateOtpCommand.builder()
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.SMS)
                .recipient("test@test.com")
                .metadata("{}")
                .build();
        when(otpProperties.getCodeLength()).thenReturn(6);
        when(otpProperties.getExpirationMinutes()).thenReturn(1);
        when(otpProperties.getMaxAttempts()).thenReturn(3);
    }

    @Test
    void generate_givenValidCommand_thenShouldSucceed() {
        when(otpGenerator.generateCode(anyInt())).thenReturn("123456");
        otpService.generate(mockedCommand);

        ArgumentCaptor<OtpTransaction> otpArgumentCaptor = ArgumentCaptor.forClass(OtpTransaction.class);
        verify(otpTransactionRepository, times(1)).saveAndFlush(otpArgumentCaptor.capture());
        OtpTransaction otpTransactionArgumentCaptorValue = otpArgumentCaptor.getValue();

        // Assertions for the Otp object
        assertEquals("123456", otpTransactionArgumentCaptorValue.otpCode(), "OTP code should match generated code");
        assertEquals(mockedCommand.recipient(), otpTransactionArgumentCaptorValue.recipient(), "Recipient should match command");
        assertEquals(mockedCommand.purpose(), otpTransactionArgumentCaptorValue.purpose(), "Purpose should match command");
        assertEquals(OtpStatus.ACTIVE, otpTransactionArgumentCaptorValue.status(), "Status should match command");
        assertEquals(mockedCommand.deliveryMethod(), otpTransactionArgumentCaptorValue.deliveryMethod(), "Delivery method should match command");
        assertEquals(0, otpTransactionArgumentCaptorValue.attemptCount(), "Attempt count should be 0");
        assertNotNull(otpTransactionArgumentCaptorValue.createdAt(), "Created at should not be null");
        assertNotNull(otpTransactionArgumentCaptorValue.expiresAt(), "Expires at should not be null");
        assertEquals(
                otpTransactionArgumentCaptorValue.createdAt().plusMinutes(otpProperties.getExpirationMinutes()),
                otpTransactionArgumentCaptorValue.expiresAt(),
                "Expires at should be created at plus expiration minutes"
        );
        assertNull(otpTransactionArgumentCaptorValue.id(), "ID should not be null");
        assertEquals("{}", otpTransactionArgumentCaptorValue.metadata(), "Metadata should not be null");

        verify(otpTransactionRepository, times(1)).retrieveByRecipientAndActiveStatusAndNotExpired(mockedCommand.recipient());
        verify(otpClientMap, times(1)).get(DeliveryMethod.SMS);
        verify(smsOtpClient, times(1)).send(any());
    }

    @Test
    void generate_givenExistingValidOtp_thenShouldReturnExistingOtpAndSuceed() {
        OtpTransaction otpTransaction = generateOtp("test@test.com", "123456");
        when(otpTransactionRepository.retrieveByRecipientAndActiveStatusAndNotExpired(mockedCommand.recipient()))
                .thenReturn(Optional.of(otpTransaction));
        OtpTransaction result = otpService.generate(mockedCommand);

        // Assertions for the Otp object
        assertEquals("123456", result.otpCode(), "OTP code should match generated code");
        assertEquals(otpTransaction.recipient(), result.recipient(), "Recipient should match command");
        assertEquals(otpTransaction.purpose(), result.purpose(), "Purpose should match command");
        assertEquals(OtpStatus.ACTIVE, result.status(), "Status should match command");
        assertEquals(otpTransaction.deliveryMethod(), result.deliveryMethod(), "Delivery method should match command");
        assertEquals(0, result.attemptCount(), "Attempt count should be 0");
        assertNotNull(result.createdAt(), "Created at should not be null");
        assertNotNull(result.expiresAt(), "Expires at should not be null");
        assertEquals(
                result.createdAt().plusMinutes(otpProperties.getExpirationMinutes()),
                result.expiresAt(),
                "Expires at should be created at plus expiration minutes"
        );
        assertEquals(otpTransaction.id(), result.id(), "ID should match existing ID");
        assertEquals("test metadata", result.metadata(), "Metadata should not be null");

        verify(otpTransactionRepository, times(1)).retrieveByRecipientAndActiveStatusAndNotExpired(mockedCommand.recipient());
        verifyNoInteractions(otpClientMap, otpGenerator, smsOtpClient);
    }

    @Test
    void generate_givenDeliveryMethodIsNotSupported_thenShouldThrowException() {
        when(otpGenerator.generateCode(anyInt())).thenReturn("123456");

        assertThrows(UnsupportedDeliveryMethodException.class, () -> {
            otpService.generate(mockedCommand.toBuilder()
                    .deliveryMethod(DeliveryMethod.PUSH)
                    .build());
        }, " Should throw UnsupportedDeliveryMethodException when delivery method is not supported");

        verify(otpTransactionRepository, times(1)).retrieveByRecipientAndActiveStatusAndNotExpired(mockedCommand.recipient());
        verify(otpClientMap, times(1)).get(DeliveryMethod.PUSH);
    }

    @Test
    void verify_givenValidOtpCode_thenShouldSucceed() {
        OtpTransaction otpTransaction = spy(generateOtp("test@test.com", "123456"));
        when(otpTransactionRepository.retrieveById(otpTransaction.id())).thenReturn(Optional.of(otpTransaction));
        when(otpTransaction.status()).thenReturn(OtpStatus.USED);
        when(otpTransactionRepository.save(any(OtpTransaction.class))).thenReturn(otpTransaction);

        otpService.verify(VerifyOtpCommand.builder()
                .id(otpTransaction.id())
                .otpCode("123456")
                .build());

        verify(otpTransactionRepository, times(1)).retrieveById(otpTransaction.id());
        verify(otpTransactionRepository, times(1)).save(any());
    }

    @Test
    void verify_givenNonExistingOtpCode_thenShouldThrowException() {
        when(otpTransactionRepository.retrieveById(any(UUID.class))).thenReturn(Optional.empty());

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(UUID.randomUUID())
                .otpCode("123456")
                .build();

        assertThrows(OtpNotFoundException.class,
                () -> otpService.verify(command),
                "Should throw OtpNotFoundException when OTP ID is not found");

        verify(otpTransactionRepository, never()).save(any());
    }

    @Test
    void verify_givenIncorrectOtpCode_thenShouldIncrementAttempts() {
        OtpTransaction otpTransaction = generateOtp("test@test.com", "654321");
        when(otpTransactionRepository.retrieveById(otpTransaction.id())).thenReturn(Optional.of(otpTransaction));
        when(otpTransactionRepository.save(any(OtpTransaction.class))).thenReturn(otpTransaction);

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otpTransaction.id())
                .otpCode("123456")
                .build();

        assertThrows(OtpVerificationException.class,
                () -> otpService.verify(command),
                "Should throw OtpVerificationException when OTP code is incorrect");

        verify(otpTransactionRepository, times(1)).save(argThat(savedOtp ->
                savedOtp.attemptCount() == 1 && savedOtp.updatedAt() != null));
    }

    @Test
    void verify_givenMaxAttemptsExceeded_thenShouldThrowException() {
        OtpTransaction otpTransaction = spy(generateOtp("test@test.com", "654321").toBuilder()
                .attemptCount(2)
                .build());
        when(otpTransactionRepository.retrieveById(otpTransaction.id())).thenReturn(Optional.of(otpTransaction));
        when(otpProperties.getMaxAttempts()).thenReturn(3);
        when(otpTransaction.status()).thenReturn(OtpStatus.INVALIDATED);
        when(otpTransactionRepository.save(any(OtpTransaction.class))).thenReturn(otpTransaction);

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otpTransaction.id())
                .otpCode("123456")
                .build();

        assertThrows(OtpAttemptLimitExceededException.class,
                () -> otpService.verify(command),
                "Should throw OtpAttemptLimitExceededException when max attempts are reached");

        verify(otpTransactionRepository, times(1))
                .save(argThat(savedOtp -> savedOtp.attemptCount() == 3 && savedOtp.updatedAt() != null));
    }



    @Test
    void verify_givenOtpIsAlreadyVerified_thenShouldThrowException() {
        OtpTransaction otpTransaction = spy(generateOtp("test@test.com", "123456").toBuilder()
                .attemptCount(2)
                .build());
        when(otpTransactionRepository.retrieveById(otpTransaction.id())).thenReturn(Optional.of(otpTransaction));
        when(otpTransaction.isActive()).thenReturn(false);

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otpTransaction.id())
                .otpCode("123456")
                .build();

        OtpInvalidStateException otpAlreadyVerifiedException = assertThrows(OtpInvalidStateException.class,
                () -> otpService.verify(command),
                "Should throw OtpInvalidStateException when OTP is already verified");

        verify(otpTransactionRepository, times(1)).retrieveById(otpTransaction.id());
        verify(otpTransactionRepository, never()).save(any(OtpTransaction.class));
    }

    @Test
    void verify_givenExpiredOtp_thenShouldThrowException() {
        OtpTransaction otpTransaction = generateOtp("test@test.com", "123456").toBuilder()
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        when(otpTransactionRepository.retrieveById(otpTransaction.id())).thenReturn(Optional.of(otpTransaction));
        when(otpTransactionRepository.save(any(OtpTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otpTransaction.id())
                .otpCode("123456")
                .build();

        assertThrows(OtpExpiredException.class,
                () -> otpService.verify(command),
                "Should throw OtpExpiredException when OTP has expired");

        verify(otpTransactionRepository, times(1)).retrieveById(otpTransaction.id());
        verify(otpTransactionRepository, times(1)).save(any(OtpTransaction.class));
    }

    @Test
    void retrieveById_givenValidOtpId_thenShouldReturnOtp() {
        OtpTransaction otpTransaction = generateOtp("test@test.com", "123456");
        when(otpTransactionRepository.retrieveById(otpTransaction.id())).thenReturn(Optional.of(otpTransaction));

        OtpTransaction result = otpService.retrieveById(otpTransaction.id());

        assertEquals(otpTransaction, result, "Should return the correctly retrieved OTP");
        verify(otpTransactionRepository, times(1)).retrieveById(otpTransaction.id());
    }

    @Test
    void retrieveById_givenNonExistingOtpId_thenShouldThrowException() {
        UUID randomId = UUID.randomUUID();
        when(otpTransactionRepository.retrieveById(randomId)).thenReturn(Optional.empty());

        assertThrows(OtpNotFoundException.class, () -> otpService.retrieveById(randomId),
                "Should throw OtpNotFoundException when OTP does not exist");

        verify(otpTransactionRepository, times(1)).retrieveById(randomId);
    }

    private static OtpTransaction generateOtp(String recipient, String otpCode) {
        OffsetDateTime now = OffsetDateTime.now();
        return OtpTransaction.builder()
                .id(UUID.randomUUID())
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipient(recipient)
                .status(OtpStatus.ACTIVE)
                .metadata("test metadata")
                .attemptCount(0)
                .createdAt(now)
                .expiresAt(now.plusMinutes(1))
                .otpCode(otpCode)
                .build();
    }
}