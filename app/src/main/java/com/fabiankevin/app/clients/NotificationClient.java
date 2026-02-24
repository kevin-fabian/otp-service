package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;

import java.util.concurrent.CompletableFuture;

public interface NotificationClient {
    void send(OneTimePasswordTransaction oneTimePasswordTransaction);
    CompletableFuture<Void> sendAsync(OneTimePasswordTransaction oneTimePasswordTransaction);
    DeliveryMethod supports();
}
