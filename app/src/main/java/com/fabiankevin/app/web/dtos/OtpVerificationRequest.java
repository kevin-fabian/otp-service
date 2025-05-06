package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.services.commands.VerifyOtpCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
@Schema(description = "Request object for OTP verification")
public record OtpVerificationRequest(
        @Schema(description = "The OTP code to verify", example = "123456")
        @NotBlank(message = "OTP code must not be blank")
        String otpCode,

        @Schema(description = "The reference ID received during OTP generation")
        @NotBlank(message = "Reference ID must not be blank")
        UUID referenceId
) {
    public VerifyOtpCommand toCommand() {
        return new VerifyOtpCommand(
                this.referenceId,
                this.otpCode
        );
    }
}
