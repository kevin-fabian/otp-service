package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.Otp;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSMSOtpClient implements OtpClient {
    @Override
    public void send(Otp otp) {
        log.info("Sending SMS to {}", otp.userIdentifier());
    }
}
