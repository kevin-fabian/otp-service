package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.Otp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailOtpClient implements OtpClient {
    @Override
    public void send(Otp otp) {
        log.info("Sending OTP to {}", otp.userIdentifier());
    }
}
