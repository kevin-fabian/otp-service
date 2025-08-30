package com.fabiankevin.app.web.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
@Schema(description = "Request object for OTP verification")
public record OtpVerificationRequest(
        @JsonProperty("otp_code")
        @Schema(description = "The OTP code to verify", example = "123456")
        @Size(min = 4, max = 6, message = "OTP code must be a string up to 6 digits")
        String otpCode) {
}
