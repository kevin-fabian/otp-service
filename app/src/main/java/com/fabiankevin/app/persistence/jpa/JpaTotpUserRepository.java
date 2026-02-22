package com.fabiankevin.app.persistence.jpa;

import com.fabiankevin.app.persistence.entities.TotpUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaTotpUserRepository extends JpaRepository<TotpUserEntity, UUID> {
    Optional<TotpUserEntity> findByUserId(String userId);
}
