package com.fabiankevin.app.web;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.TotpService;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;
import com.fabiankevin.app.web.dtos.RegisterTotpRequest;
import com.fabiankevin.app.web.dtos.TotpResponse;
import com.fabiankevin.app.web.dtos.VerifyOtpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/totp")
@RequiredArgsConstructor
public class TotpController {
    private final TotpService totpService;

    @PostMapping("/users/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public TotpResponse register(@Valid @RequestBody RegisterTotpRequest request) {
        TotpUser totpUser = totpService.registerTotp(new RegisterTotpCommand(request.userReferenceId()));
        return new TotpResponse(totpUser.userReferenceId());
    }

    @GetMapping(value = "/qr/{userReferenceId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable String userReferenceId) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(totpService.getQrCodeImageByUserReferenceId(userReferenceId));
    }

    @PostMapping("/verification")
    public void verify(@Valid @RequestBody VerifyOtpRequest request) {
        totpService.verifyTotp(request.userReferenceId(), request.code());
    }
}
