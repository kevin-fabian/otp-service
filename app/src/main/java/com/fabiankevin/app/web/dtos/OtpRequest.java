package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder(toBuilder = true)
public record OtpRequest(
        @NotBlank(message = "User identifier is required")
        @JsonProperty("user_identifier")
        @Schema(description = "User identifier could be an email or phone number",
                examples = {
                        "test@example.com",
                        "+639000000"
                },
                maxLength = 128)
        String userIdentifier,
        @NotNull(message = "OTP purpose must be specified")
        OtpPurpose purpose,
        @NotNull(message = "Delivery method must be specified")
        @JsonProperty("delivery_method")
        DeliveryMethod deliveryMethod,
        @NotBlank(message = "Metadata if provided must not be empty")
        String metadata
) {
    public GenerateOtpCommand toCommand() {
        return GenerateOtpCommand.builder()
                .userIdentifier(this.userIdentifier)
                .purpose(this.purpose)
                .deliveryMethod(this.deliveryMethod)
                .metadata(this.metadata)
                .build();
    }
}