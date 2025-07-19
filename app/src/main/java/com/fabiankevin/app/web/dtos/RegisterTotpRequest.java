package com.fabiankevin.app.web.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterTotpRequest(
        @NotBlank(message = "User reference ID must not be blank")
        @Size(min = 4, max = 128, message = "User reference ID must be a string up to 128 characters")
        String userReferenceId) {
}
