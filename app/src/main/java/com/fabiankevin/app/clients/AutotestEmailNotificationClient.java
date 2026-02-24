package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Profile("autotest")
@Slf4j
public class AutotestEmailNotificationClient implements NotificationClient {
    @Override
    public void send(OneTimePasswordTransaction oneTimePasswordTransaction) {
        log.info("Otp sent to {}: {} (local mode)", oneTimePasswordTransaction.recipient(), oneTimePasswordTransaction.otpCode());
    }

    @Override
    public CompletableFuture<Void> sendAsync(OneTimePasswordTransaction oneTimePasswordTransaction) {
        return CompletableFuture.runAsync(() -> send(oneTimePasswordTransaction));
    }

    @Override
    public DeliveryMethod supports() {
        return DeliveryMethod.EMAIL;
    }
}
