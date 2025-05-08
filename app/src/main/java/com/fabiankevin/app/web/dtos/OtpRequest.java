package com.fabiankevin.app.web.dtos;

import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.services.commands.GenerateOtpCommand;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record OtpRequest(
        @NotBlank(message = "User identifier is required")
        @Size(min = 1, max = 128, message = "User identifier must be a string up to 128 characters")
        @JsonProperty("user_identifier")
        @Schema(description = "User identifier could be an email or phone number",
                examples = {
                        "+639000000000",
                        "test@example.com",
                },
                maxLength = 128)
        String userIdentifier,
        @NotNull(message = "OTP purpose must be specified")
        OtpPurpose purpose,
        @NotNull(message = "Delivery method must be specified")
        @JsonProperty("delivery_method")
        DeliveryMethod deliveryMethod,
        @Size(min = 0, max = 1024, message = "Metadata must be a string up to 1024 characters")
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