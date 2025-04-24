package com.fabiankevin.app.services;

import com.fabiankevin.app.clients.OtpClient;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class DefaultOtpServiceTest {
    private final OtpRepository otpRepository = mock(OtpRepository.class);
    private final OtpClient otpClient = mock(OtpClient.class);
    private final OtpService otpService = new DefaultOtpService(otpRepository, otpClient);

    @Test
    void generate_given_then(){
        GenerateOtpCommand command = GenerateOtpCommand.builder()
                .build();
        otpService.generate(command);
    }
}