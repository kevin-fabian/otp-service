package com.fabiankevin.app.web;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.totp.TotpService;
import com.fabiankevin.app.services.totp.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.totp.commands.VerifyTotpCommand;
import com.fabiankevin.app.web.dtos.TotpResponse;
import com.fabiankevin.app.web.dtos.VerifyOtpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/totp/users")
@RequiredArgsConstructor
public class TotpController {
    private final TotpService totpService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a user to Time-based One-Time Password (TOTP)",
            description = "Registers a new TOTP user based from JWT",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created - Successfully registered a user to TOTP service",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TotpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User input is invalid"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public TotpResponse register(@AuthenticationPrincipal Jwt jwt) {
        TotpUser totpUser = totpService.registerTotp(new RegisterTotpCommand(jwt.getSubject()));
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

    @PostMapping("/{userReferenceId}/verify")
    @Operation(summary = "Verify TOTP",
            description = "Verifies the provided TOTP code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Successfully verified TOTP"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User input is invalid"),
                    @ApiResponse(responseCode = "404", description = "Not Found - TOTP user not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public void verify(
            @Parameter(description = "User reference Id")
            @PathVariable String userReferenceId,
            @Parameter(description = "TOTP verification request")
            @Valid @RequestBody VerifyOtpRequest request) {
        totpService.verify(VerifyTotpCommand.builder()
                        .userReferenceId(userReferenceId)
                        .code(request.code())
                        .purpose(request.purpose())
                .build());
    }
}
