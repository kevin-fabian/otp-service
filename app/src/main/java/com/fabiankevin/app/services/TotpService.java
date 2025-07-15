package com.fabiankevin.app.services;

import com.fabiankevin.app.models.Totp;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;

public interface TotpService {
    Totp registerTotp(RegisterTotpCommand command);
}
