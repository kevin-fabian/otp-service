package com.fabiankevin.app.web.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response object")
public class ApiErrorResponse {
    @Schema(description = "Main error message", example = "Validation Failed")
    private String message;

    @Schema(description = "Detailed error description", example = "Invalid request data")
    private String details;

    @Schema(description = "Timestamp of when the error occurred", example = "2025-03-01T12:00:00Z")
    private Instant timestamp;

    @Schema(description = "List of validation errors", example = "[\"Name is required\"]")
    private List<String> errors;

    public ApiErrorResponse(String message, String details) {
        this.message = message;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public ApiErrorResponse(String message, List<String> errors) {
        this.message = message;
        this.errors = errors;
        this.timestamp = Instant.now();
    }
}