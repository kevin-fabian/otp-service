package com.fabiankevin.app.models;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
public record TotpUser(UUID id,
                       String userReferenceId,
                       String secret,
                       Instant createdAt,
                       Instant updatedAt) {
}
