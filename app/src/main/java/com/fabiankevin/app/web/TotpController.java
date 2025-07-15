package com.fabiankevin.app.web;

import com.fabiankevin.app.web.dtos.RegisterTotpRequest;
import com.fabiankevin.app.web.dtos.TotpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/totp")
public class TotpController {

    @PostMapping
    public TotpResponse register(RegisterTotpRequest request) {
        // This method should handle the registration of TOTP credentials
        // For now, we return a placeholder response
        return new TotpResponse("");
    }
}
