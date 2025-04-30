package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

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
    public void setup(){
        mockedCommand = GenerateOtpCommand.builder()
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.SMS)
                .userIdentifier("test@test.com")
                .metadata("{}")
                .build();
        when(otpProperties.getOtpLength()).thenReturn(6);
        when(otpProperties.getExpiresInMinutes()).thenReturn(1);
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
        assertEquals(mockedCommand.userIdentifier(), otpArgumentCaptorValue.userIdentifier(), "User identifier should match command");
        assertEquals(mockedCommand.purpose(), otpArgumentCaptorValue.purpose(), "Purpose should match command");
        assertEquals(OtpStatus.ACTIVE, otpArgumentCaptorValue.status(), "Status should match command");
        assertEquals(mockedCommand.deliveryMethod(), otpArgumentCaptorValue.deliveryMethod(), "Delivery method should match command");
        assertEquals(0, otpArgumentCaptorValue.attemptCount(), "Attempt count should be 0");
        assertNotNull(otpArgumentCaptorValue.createdAt(), "Created at should not be null");
        assertNotNull(otpArgumentCaptorValue.expiresAt(), "Expires at should not be null");
        assertEquals(
                otpArgumentCaptorValue.createdAt().plusMinutes(otpProperties.getExpiresInMinutes()),
                otpArgumentCaptorValue.expiresAt(),
                "Expires at should be created at plus expiration minutes"
        );
        assertNull(otpArgumentCaptorValue.id(), "ID should not be null");
        assertEquals("{}", otpArgumentCaptorValue.metadata(), "Metadata should not be null");

        verify(otpRepository, times(1)).existByUserIdentifierAndStatusActive(mockedCommand.userIdentifier());
        verify(otpClientMap, times(1)).get(DeliveryMethod.SMS);
        verify(smsOtpClient, times(1)).send(any());
    }
}