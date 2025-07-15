package com.fabiankevin.app.services;

import com.fabiankevin.app.models.Totp;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@RequiredArgsConstructor
@Service
public class DefaultTotpService implements TotpService {



    @Override
    public Totp registerTotp(RegisterTotpCommand command) {

        return null;
    }

    private String generateSecureRandomSecret() {
        SecureRandom random = new SecureRandom();
        random.nextBytes(new byte[20]);

        return "secure";
    }
}
