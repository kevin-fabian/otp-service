package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.OtpTransaction;

import java.util.concurrent.CompletableFuture;

public interface OtpClient {
    void send(OtpTransaction otpTransaction);
    CompletableFuture<Void> sendAsync(OtpTransaction otpTransaction);
}
