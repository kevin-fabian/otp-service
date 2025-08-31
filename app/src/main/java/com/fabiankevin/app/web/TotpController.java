package com.fabiankevin.app.web;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.services.TotpService;
import com.fabiankevin.app.services.commands.RegisterTotpCommand;
import com.fabiankevin.app.services.commands.VerifyTotpCommand;
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

import java.util.UUID;

@RestController
@RequestMapping("/v1/totp")
@RequiredArgsConstructor
public class TotpController {
    private final TotpService totpService;

    @PostMapping("/users/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a user to Time-based One-Time Password (TOTP)",
            description = "Registers a new TOTP for the provided user reference ID.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created - Successfully registered a user to TOTP service",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = TotpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User input is invalid"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public TotpResponse register(
            @Parameter(description = "TOTP registration request")
            @Valid @RequestBody RegisterTotpRequest request) {
        TotpUser totpUser = totpService.registerTotp(new RegisterTotpCommand(request.userReferenceId()));
        return new TotpResponse(totpUser.id());
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get QR Code",
            description = "Generates QR code image for TOTP setup.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK -Successfully generated QR code",
                            content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Not Found - User reference not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public ResponseEntity<byte[]> getQrCodeImage(
            @Parameter(description = "User reference ID")
            @RequestParam("userReferenceId") String userReferenceId) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(totpService.getQrCodeImageByUserReferenceId(userReferenceId));
    }

    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify TOTP",
            description = "Verifies the provided TOTP code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - Successfully verified TOTP"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User input is invalid"),
                    @ApiResponse(responseCode = "404", description = "Not Found - TOTP user not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public void verify(
            @PathVariable UUID id,
            @Parameter(description = "TOTP verification request")
            @Valid @RequestBody VerifyOtpRequest request) {
        totpService.verify(VerifyTotpCommand.builder()
                        .id(id)
                        .code(request.code())
                        .purpose(request.purpose())
                .build());
    }
}
