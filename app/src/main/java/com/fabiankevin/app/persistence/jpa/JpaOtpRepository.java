package com.fabiankevin.app.persistence.jpa;

import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface JpaOtpRepository extends JpaRepository<OtpEntity, UUID> {
    boolean existsByUserIdentifierAndStatus(String userIdentifier, OtpStatus status);

    Optional<OtpEntity> findByUserIdentifierAndStatusAndExpiresAtGreaterThan(String userIdentifier, OtpStatus status, OffsetDateTime now);
}