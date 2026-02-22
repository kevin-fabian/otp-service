package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.entities.OtpTransactionEntity;
import com.fabiankevin.app.persistence.jpa.JpaOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultOtpTransactionRepository implements OtpTransactionRepository {
    private final JpaOtpRepository jpaOtpRepository;

    @Override
    public OtpTransaction saveAndFlush(OtpTransaction otpTransaction) {
        return jpaOtpRepository.saveAndFlush(OtpTransactionEntity.fromModel(otpTransaction)).toModel();
    }

    @Override
    public OtpTransaction save(OtpTransaction otpTransaction) {
        return jpaOtpRepository.save(OtpTransactionEntity.fromModel(otpTransaction)).toModel();
    }

    @Override
    public Optional<OtpTransaction> retrieveById(UUID id) {
        return jpaOtpRepository.findById(id)
                .map(OtpTransactionEntity::toModel);
    }

    @Override
    public Optional<OtpTransaction> retrieveByRecipientAndActiveStatus(String recipient) {
        return jpaOtpRepository.findByRecipientAndStatus(recipient, OtpStatus.VERIFIED)
                .map(OtpTransactionEntity::toModel);
    }

    @Override
    public Optional<OtpTransaction> retrieveByRecipientAndStatus(String recipient, List<OtpStatus> otpStatuses) {
        return jpaOtpRepository.findByRecipientAndStatusIn(recipient, otpStatuses)
                .map(OtpTransactionEntity::toModel);
    }
}