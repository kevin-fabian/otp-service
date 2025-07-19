package com.fabiankevin.app.services;

import com.fabiankevin.app.services.commands.GenerateQrCodeCommand;

public interface QrGenerator {
    byte[] generate(GenerateQrCodeCommand command);
}
