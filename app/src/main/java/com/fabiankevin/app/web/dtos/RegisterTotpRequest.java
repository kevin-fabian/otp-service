package com.fabiankevin.app.web.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for TOTP user registration")
public record RegisterTotpRequest(
        @NotBlank(message = "User reference ID must not be blank")
        @Size(min = 4, max = 128, message = "User reference ID must be a string up to 128 characters")
        @Schema(description = "The unique reference ID of the user", example = "john.doe@example.com")
        String userReferenceId) {
}
