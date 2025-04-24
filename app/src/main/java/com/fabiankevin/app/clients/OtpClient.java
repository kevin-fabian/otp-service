package com.fabiankevin.app.clients;

import com.fabiankevin.app.models.Otp;

public interface OtpClient {
    void send(Otp otp);
}
