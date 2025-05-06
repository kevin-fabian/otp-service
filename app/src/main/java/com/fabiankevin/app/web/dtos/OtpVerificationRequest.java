package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.services.commands.VerifyOtpCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
@Schema(description = "Request object for OTP verification")
public record OtpVerificationRequest(
        @Schema(description = "The OTP code to verify", example = "123456")
        @Size(min = 4, max = 6, message = "OTP code must be a string up to 6 digits")
        String otpCode,

        @Schema(description = "The reference ID received during OTP generation")
        @NotNull(message = "Reference ID must not be null")
        UUID referenceId
) {
    public VerifyOtpCommand toCommand() {
        return new VerifyOtpCommand(
                this.referenceId,
                this.otpCode
        );
    }
}
