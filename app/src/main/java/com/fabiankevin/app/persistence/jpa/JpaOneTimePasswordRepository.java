package com.fabiankevin.app.persistence.jpa;

import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.entities.OneTimePasswordTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOneTimePasswordRepository extends JpaRepository<OneTimePasswordTransactionEntity, UUID> {
    Optional<OneTimePasswordTransactionEntity> findByRecipientAndStatus(String recipient, OtpStatus status);
    Optional<OneTimePasswordTransactionEntity> findByRecipientAndStatusIn(String recipient, List<OtpStatus> statuses);
}