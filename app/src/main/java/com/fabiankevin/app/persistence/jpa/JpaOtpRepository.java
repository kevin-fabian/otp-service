package com.fabiankevin.app.persistence.jpa;

import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.entities.OtpTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOtpRepository extends JpaRepository<OtpTransactionEntity, UUID> {
    Optional<OtpTransactionEntity> findByRecipientAndStatus(String recipient, OtpStatus status);
    Optional<OtpTransactionEntity> findByRecipientAndStatusIn(String recipient, List<OtpStatus> statuses);
}