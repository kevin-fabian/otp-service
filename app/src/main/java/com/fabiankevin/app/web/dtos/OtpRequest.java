package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record OtpRequest(
        @NotBlank(message = "Recipient is required")
        @Size(min = 1, max = 128, message = "Recipient must be a string up to 128 characters")
        @Schema(description = "Recipient could be an email or phone number",
                examples = {
                        "+639000000000",
                        "test@example.com",
                },
                maxLength = 128)
        String recipient,
        @Schema(description = "The purpose of the OTP", example = "LOGIN")
        @NotNull(message = "OTP purpose must be specified")
        OtpPurpose purpose,
        @NotNull(message = "Delivery method must be specified")
        @Schema(description = "The method of OTP delivery", example = "SMS")
        DeliveryMethod deliveryMethod,
        @Size(max = 1024, message = "Metadata must be a string up to 1024 characters")
        @Schema(description = "Optional metadata for the OTP", maxLength = 1024)
        String metadata) {
    public GenerateOtpCommand toCommand() {
        return GenerateOtpCommand.builder()
                .recipient(this.recipient)
                .purpose(this.purpose)
                .deliveryMethod(this.deliveryMethod)
                .metadata(this.metadata)
                .build();
    }
}