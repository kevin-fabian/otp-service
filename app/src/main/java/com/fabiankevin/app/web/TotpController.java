package com.fabiankevin.app.web;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.TotpService;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;
import com.fabiankevin.app.web.dtos.RegisterTotpRequest;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/totp")
@RequiredArgsConstructor
public class TotpController {
    private final TotpService totpService;

    @PostMapping("/users/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register TOTP",
            description = "Registers a new TOTP for the provided user reference.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully registered TOTP",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TotpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            })
    public TotpResponse register(
            @Parameter(description = "TOTP registration request")
            @Valid @RequestBody RegisterTotpRequest request) {
        TotpUser totpUser = totpService.registerTotp(new RegisterTotpCommand(request.userReferenceId()));
        return new TotpResponse(totpUser.userReferenceId());
    }

    @GetMapping(value = "/qr/{userReferenceId}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get QR Code",
            description = "Generates QR code image for TOTP setup.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully generated QR code",
                            content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
                    @ApiResponse(responseCode = "404", description = "User reference not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            })
    public ResponseEntity<byte[]> getQrCodeImage(
            @Parameter(description = "User reference ID")
            @PathVariable String userReferenceId) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(totpService.getQrCodeImageByUserReferenceId(userReferenceId));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify TOTP",
            description = "Verifies the provided TOTP code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully verified TOTP"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "404", description = "TOTP user not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            })
    public void verify(
            @Parameter(description = "TOTP verification request")
            @Valid @RequestBody VerifyOtpRequest request) {
        totpService.verifyTotp(request.userReferenceId(), request.code());
    }
}
