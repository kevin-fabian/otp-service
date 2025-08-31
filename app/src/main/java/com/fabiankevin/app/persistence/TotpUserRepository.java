package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.TotpUser;

import java.util.Optional;
import java.util.UUID;

public interface TotpUserRepository {
    TotpUser save(TotpUser totpUser);
    Optional<TotpUser> findByUserReferenceId(String userReferenceId);
    Optional<TotpUser> findById(UUID id);
}
