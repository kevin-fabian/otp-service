package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.Otp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local-h2")
@Slf4j
// This class is used in local development to avoid sending real emails.
public class LocalEmailOtpClient implements OtpClient {
    @Override
    public void send(Otp otp) {
        log.info("Otp sent to {}: {} (local mode)", otp.recipient(), otp.otpCode());
    }
}
