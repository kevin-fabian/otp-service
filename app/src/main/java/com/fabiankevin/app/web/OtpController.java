package com.fabiankevin.app.web;

import com.fabiankevin.app.services.OtpService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;


    @PostMapping
    @Operation(summary = "Generate OTP",
            description = "Generates a new OTP based on the provided request.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully generated OTP",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OtpResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            })
    public OtpResponse generateOtp(
            @Parameter(description = "Otp code request")
            @Valid
            @RequestBody
            OtpRequest otpRequest) {
        return OtpResponse.from(otpService.generate(otpRequest.toCommand()));
    }

    @PostMapping("/verification")
    @Operation(summary = "Verify OTP",
            description = "Verifies the provided OTP code.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully verified OTP"),
                    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
                    @ApiResponse(responseCode = "404", description = "OTP not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
            })
    public void verifyOtp(
            @Parameter(description = "Otp verification request")
            @Valid
            @RequestBody
            OtpVerificationRequest request) {
        otpService.verify(request.toCommand());
    }
}
