package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Profile("autotest")
@Slf4j
public class AutotestEmailOtpClient implements OtpClient {
    @Override
    public void send(OtpTransaction otpTransaction) {
        log.info("Otp sent to {}: {} (local mode)", otpTransaction.recipient(), otpTransaction.otpCode());
    }

    @Override
    public CompletableFuture<Void> sendAsync(OtpTransaction otpTransaction) {
        return CompletableFuture.runAsync(() -> send(otpTransaction));
    }

    @Override
    public DeliveryMethod supports() {
        return DeliveryMethod.EMAIL;
    }
}
