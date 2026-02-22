package com.fabiankevin.app.web;

import com.fabiankevin.app.services.otp.OtpService;
import com.fabiankevin.app.services.otp.commands.VerifyOtpCommand;
import com.fabiankevin.app.web.dtos.OtpRequest;
import com.fabiankevin.app.web.dtos.OtpResponse;
import com.fabiankevin.app.web.dtos.OtpVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/otps")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Generate an One-Time Password (OTP)",
            description = "Generates a new OTP based on the provided request.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created - Successfully generated OTP",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OtpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User input is invalid"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error - An error occurred on the server")
            })
    public OtpResponse generateOtp(
            @Parameter(description = "Otp code request")
            @Valid
            @RequestBody
            OtpRequest otpRequest) {
        return OtpResponse.from(otpService.generate(otpRequest.toCommand()));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{otpId}/verify")
    @Operation(summary = "Verify OTP",
            description = "Verifies the provided OTP code.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "NO Content - Successfully verified OTP"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - User input is invalid"),
                    @ApiResponse(responseCode = "429", description = "Too Many Requests -  OTP attempt limit exceeded"),
                    @ApiResponse(responseCode = "404", description = "Not Found - OTP not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public void verifyOtp(
            @PathVariable UUID otpId,
            @Parameter(description = "Otp verification request")
            @Valid
            @RequestBody
            OtpVerificationRequest request) {
        otpService.verify(new VerifyOtpCommand(otpId, request.otpCode()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve OTP by ID",
            description = "Retrieves the OTP details by ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - OTP retrieved successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OtpResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not Found - Otp not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public OtpResponse retrieveById(@PathVariable UUID id) {
        return OtpResponse.from(otpService.retrieveById(id));
    }

    @PatchMapping("/{id}/mark-as-used")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Patch OTP status as used",
            description = "Updates the OTP status to used.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content - OTP status is set to USED successfully"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Otp not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error - An error occurred on the server")
            })
    public void markOtpAsUsed(@PathVariable UUID id) {
        otpService.useOtp(id);
    }
}
