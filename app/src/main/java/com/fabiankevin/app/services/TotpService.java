package com.fabiankevin.app.services;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.commands.VerifyTotpCommand;

public interface TotpService {
    TotpUser registerTotp(RegisterTotpCommand command);
    byte[] getQrCodeImageByUserReferenceId(String id);
    void verify(VerifyTotpCommand command);
}
