package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.exceptions.*;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fabiankevin.app.services.commands.VerifyOtpCommand;
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

class DefaultOtpServiceTest {
    private final OtpRepository otpRepository = mock(OtpRepository.class);
    private final OtpClient smsOtpClient = mock(OtpClient.class);
    private final Map<DeliveryMethod, OtpClient> otpClientMap = spy(
            Map.of(DeliveryMethod.SMS, smsOtpClient)
    );
    private final OtpGenerator otpGenerator = mock(OtpGenerator.class);
    private final OtpProperties otpProperties = mock(OtpProperties.class);
    private final OtpService otpService = new DefaultOtpService(otpRepository, otpClientMap, otpGenerator, otpProperties);

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

        ArgumentCaptor<Otp> otpArgumentCaptor = ArgumentCaptor.forClass(Otp.class);
        verify(otpRepository, times(1)).saveAndFlush(otpArgumentCaptor.capture());
        Otp otpArgumentCaptorValue = otpArgumentCaptor.getValue();

        // Assertions for the Otp object
        assertEquals("123456", otpArgumentCaptorValue.otpCode(), "OTP code should match generated code");
        assertEquals(mockedCommand.recipient(), otpArgumentCaptorValue.recipient(), "Recipient should match command");
        assertEquals(mockedCommand.purpose(), otpArgumentCaptorValue.purpose(), "Purpose should match command");
        assertEquals(OtpStatus.ACTIVE, otpArgumentCaptorValue.status(), "Status should match command");
        assertEquals(mockedCommand.deliveryMethod(), otpArgumentCaptorValue.deliveryMethod(), "Delivery method should match command");
        assertEquals(0, otpArgumentCaptorValue.attemptCount(), "Attempt count should be 0");
        assertNotNull(otpArgumentCaptorValue.createdAt(), "Created at should not be null");
        assertNotNull(otpArgumentCaptorValue.expiresAt(), "Expires at should not be null");
        assertEquals(
                otpArgumentCaptorValue.createdAt().plusMinutes(otpProperties.getExpirationMinutes()),
                otpArgumentCaptorValue.expiresAt(),
                "Expires at should be created at plus expiration minutes"
        );
        assertNull(otpArgumentCaptorValue.id(), "ID should not be null");
        assertEquals("{}", otpArgumentCaptorValue.metadata(), "Metadata should not be null");

        verify(otpRepository, times(1)).retrieveByUserIdentifierAndActiveStatusAndNotExpired(mockedCommand.recipient());
        verify(otpClientMap, times(1)).get(DeliveryMethod.SMS);
        verify(smsOtpClient, times(1)).send(any());
    }

    @Test
    void generate_givenExistingValidOtp_thenShouldReturnExistingOtpAndSuceed() {
        Otp otp = generateOtp("test@test.com", "123456");
        when(otpRepository.retrieveByUserIdentifierAndActiveStatusAndNotExpired(mockedCommand.recipient()))
                .thenReturn(Optional.of(otp));
        Otp result = otpService.generate(mockedCommand);

        // Assertions for the Otp object
        assertEquals("123456", result.otpCode(), "OTP code should match generated code");
        assertEquals(otp.recipient(), result.recipient(), "Recipient should match command");
        assertEquals(otp.purpose(), result.purpose(), "Purpose should match command");
        assertEquals(OtpStatus.ACTIVE, result.status(), "Status should match command");
        assertEquals(otp.deliveryMethod(), result.deliveryMethod(), "Delivery method should match command");
        assertEquals(0, result.attemptCount(), "Attempt count should be 0");
        assertNotNull(result.createdAt(), "Created at should not be null");
        assertNotNull(result.expiresAt(), "Expires at should not be null");
        assertEquals(
                result.createdAt().plusMinutes(otpProperties.getExpirationMinutes()),
                result.expiresAt(),
                "Expires at should be created at plus expiration minutes"
        );
        assertEquals(otp.id(), result.id(), "ID should match existing ID");
        assertEquals("test metadata", result.metadata(), "Metadata should not be null");

        verify(otpRepository, times(1)).retrieveByUserIdentifierAndActiveStatusAndNotExpired(mockedCommand.recipient());
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

        verify(otpRepository, times(1)).retrieveByUserIdentifierAndActiveStatusAndNotExpired(mockedCommand.recipient());
        verify(otpClientMap, times(1)).get(DeliveryMethod.PUSH);
    }

    @Test
    void verify_givenValidOtpCode_thenShouldSucceed() {
        Otp otp = spy(generateOtp("test@test.com", "123456"));
        when(otpRepository.retrieveById(otp.id())).thenReturn(Optional.of(otp));
        when(otp.status()).thenReturn(OtpStatus.USED);
        when(otpRepository.save(any(Otp.class))).thenReturn(otp);

        otpService.verify(VerifyOtpCommand.builder()
                .id(otp.id())
                .otpCode("123456")
                .build());

        verify(otpRepository, times(1)).retrieveById(otp.id());
        verify(otpRepository, times(1)).save(any());
    }

    @Test
    void verify_givenNonExistingOtpCode_thenShouldThrowException() {
        when(otpRepository.retrieveById(any(UUID.class))).thenReturn(Optional.empty());

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(UUID.randomUUID())
                .otpCode("123456")
                .build();

        assertThrows(OtpNotFoundException.class,
                () -> otpService.verify(command),
                "Should throw OtpNotFoundException when OTP ID is not found");

        verify(otpRepository, never()).save(any());
    }

    @Test
    void verify_givenIncorrectOtpCode_thenShouldIncrementAttempts() {
        Otp otp = generateOtp("test@test.com", "654321");
        when(otpRepository.retrieveById(otp.id())).thenReturn(Optional.of(otp));
        when(otpRepository.save(any(Otp.class))).thenReturn(otp);

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otp.id())
                .otpCode("123456")
                .build();

        assertThrows(OtpVerificationException.class,
                () -> otpService.verify(command),
                "Should throw OtpVerificationException when OTP code is incorrect");

        verify(otpRepository, times(1)).save(argThat(savedOtp ->
                savedOtp.attemptCount() == 1 && savedOtp.updatedAt() != null));
    }

    @Test
    void verify_givenMaxAttemptsExceeded_thenShouldThrowException() {
        Otp otp = spy(generateOtp("test@test.com", "654321").toBuilder()
                .attemptCount(2)
                .build());
        when(otpRepository.retrieveById(otp.id())).thenReturn(Optional.of(otp));
        when(otpProperties.getMaxAttempts()).thenReturn(3);
        when(otp.status()).thenReturn(OtpStatus.INVALIDATED);
        when(otpRepository.save(any(Otp.class))).thenReturn(otp);

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otp.id())
                .otpCode("123456")
                .build();

        assertThrows(OtpAttemptLimitExceededException.class,
                () -> otpService.verify(command),
                "Should throw OtpAttemptLimitExceededException when max attempts are reached");

        verify(otpRepository, times(1))
                .save(argThat(savedOtp -> savedOtp.attemptCount() == 3 && savedOtp.updatedAt() != null));
    }

    @Test
    void verify_givenOtpIsAlreadyVerified_thenShouldThrowException() {
        Otp otp = spy(generateOtp("test@test.com", "123456").toBuilder()
                .attemptCount(2)
                .build());
        when(otpRepository.retrieveById(otp.id())).thenReturn(Optional.of(otp));
        when(otp.isUsed()).thenReturn(true);

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otp.id())
                .otpCode("123456")
                .build();

        OtpAlreadyVerifiedException otpAlreadyVerifiedException = assertThrows(OtpAlreadyVerifiedException.class,
                () -> otpService.verify(command),
                "Should throw OtpAlreadyVerifiedException when OTP is already verified");

        assertEquals("OTP has already been used", otpAlreadyVerifiedException.getMessage(), "exception message should match");
        verify(otpRepository, times(1)).retrieveById(otp.id());
        verify(otpRepository, never()).save(any(Otp.class));
    }

    @Test
    void verify_givenExpiredOtp_thenShouldThrowException() {
        Otp otp = generateOtp("test@test.com", "123456").toBuilder()
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        when(otpRepository.retrieveById(otp.id())).thenReturn(Optional.of(otp));
        when(otpRepository.save(any(Otp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VerifyOtpCommand command = VerifyOtpCommand.builder()
                .id(otp.id())
                .otpCode("123456")
                .build();

        assertThrows(OtpExpiredException.class,
                () -> otpService.verify(command),
                "Should throw OtpExpiredException when OTP has expired");

        verify(otpRepository, times(1)).retrieveById(otp.id());
        verify(otpRepository, times(1)).save(any(Otp.class));
    }


    private static Otp generateOtp(String recipient, String otpCode) {
        OffsetDateTime now = OffsetDateTime.now();
        return Otp.builder()
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