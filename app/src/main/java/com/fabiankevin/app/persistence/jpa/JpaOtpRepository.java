package com.fabiankevin.app.persistence.jpa;

import com.fabiankevin.app.persistence.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaOtpRepository extends JpaRepository<OtpEntity, UUID> {
    boolean existByUserIdentifierAndStatusActive(String userIdentifier);
}