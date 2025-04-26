package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultOtpServiceTest {
    private final OtpRepository otpRepository = mock(OtpRepository.class);
    private final OtpClient otpClient = mock(OtpClient.class);
    private final OtpGenerator otpGenerator = mock(OtpGenerator.class);
    private final OtpService otpService = new DefaultOtpService(otpRepository, otpClient, otpGenerator);

    private GenerateOtpCommand mockedCommand;

    @BeforeEach
    public void setup(){
        mockedCommand = GenerateOtpCommand.builder()
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.SMS)
                .userIdentifier("test@test.com")
                .status(OtpStatus.ACTIVE)
                .build();
    }

    @Test
    void generate_givenValidCommand_thenShouldSucceed() {
        when(otpGenerator.generateCode(anyInt())).thenReturn("123456");
        otpService.generate(mockedCommand);
    }
}