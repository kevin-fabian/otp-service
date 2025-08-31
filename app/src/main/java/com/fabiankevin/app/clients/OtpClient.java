package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.OtpTransaction;

public interface OtpClient {
    void send(OtpTransaction otpTransaction);
}
