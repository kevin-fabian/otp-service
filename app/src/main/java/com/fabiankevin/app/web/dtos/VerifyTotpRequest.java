package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.enums.OtpPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request object for TOTP verification")
public record VerifyTotpRequest(
        @NotBlank(message = "Code must not be blank")
        @Pattern(regexp = "\\d{6}", message = "Code must be a 6-digit number")
        @Schema(description = "The 6-digit TOTP code to verify", example = "123456")
        String code,
        @NotNull(message = "OTP purpose must be specified")
        @Schema(description = "The purpose of the OTP", example = "LOGIN")
        OtpPurpose purpose) {
}
