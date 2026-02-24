package com.fabiankevin.app.web;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.services.totp.TotpService;
import com.fabiankevin.app.services.totp.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.totp.commands.VerifyTotpCommand;
import com.fabiankevin.app.web.dtos.TotpResponse;
import com.fabiankevin.app.web.dtos.VerifyTotpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/totp/users")
@RequiredArgsConstructor
public class TotpController {
    private final TotpService totpService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TotpResponse register(JwtAuthenticationToken jwtAuthenticationToken) {
        TotpUser totpUser = totpService.registerTotp(new RegisterTotpCommand(jwtAuthenticationToken.getName()));
        return new TotpResponse(totpUser.id());
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get QR Code by JWT",
            description = "Generates QR code image for TOTP setup.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Successfully generated QR code",
                            content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Not Found - User reference not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public ResponseEntity<byte[]> getQrCodeImage(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(totpService.getQrCodeImageByUserReferenceId(jwt.getSubject()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify TOTP",
            description = "Verifies the provided TOTP code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Successfully verified TOTP"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User input is invalid"),
                    @ApiResponse(responseCode = "404", description = "Not Found - TOTP user not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public void verify(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "TOTP verification request")
            @Valid @RequestBody VerifyTotpRequest request) {
        totpService.verify(VerifyTotpCommand.builder()
                        .userReferenceId(jwt.getSubject())
                        .code(request.code())
                        .purpose(OtpPurpose.LOGIN)
                .build());
    }
}
