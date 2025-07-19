package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.TotpUser;

import java.util.Optional;

public interface TotpUserRepository {
    TotpUser save(TotpUser totpUser);
    Optional<TotpUser> findByUserReferenceId(String userReferenceId);
}
