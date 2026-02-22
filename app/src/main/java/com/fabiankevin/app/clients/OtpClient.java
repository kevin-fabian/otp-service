package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;

import java.util.concurrent.CompletableFuture;

public interface OtpClient {
    void send(OtpTransaction otpTransaction);
    CompletableFuture<Void> sendAsync(OtpTransaction otpTransaction);
    DeliveryMethod supports();
}
