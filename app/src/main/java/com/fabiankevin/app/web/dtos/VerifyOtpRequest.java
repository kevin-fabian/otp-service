package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.enums.OtpPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for TOTP verification")
public record VerifyOtpRequest(
        @NotBlank(message = "Code must not be blank")
        @Size(min = 6, max = 6, message = "Code must be exactly 6 characters")
        @Schema(description = "The 6-digit TOTP code to verify", example = "123456")
        String code,
        OtpPurpose purpose) {
}
