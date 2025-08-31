package com.fabiankevin.app.services.totp;

import com.fabiankevin.app.services.totp.commands.GenerateQrCodeCommand;

public interface QrGenerator {
    byte[] generate(GenerateQrCodeCommand command);
}
