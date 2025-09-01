package com.fabiankevin.app.services.totp;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.totp.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.totp.commands.VerifyTotpCommand;

import java.util.UUID;

public interface TotpService {
    TotpUser registerTotp(RegisterTotpCommand command);
    byte[] getQrCodeImageByUserReferenceId(String id);
    byte[] getQrCodeImageById(UUID id);
    void verify(VerifyTotpCommand command);
}
