package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.entities.OneTimePasswordTransactionEntity;
import com.fabiankevin.app.persistence.jpa.JpaOneTimePasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultOtpTransactionRepository implements OtpTransactionRepository {
    private final JpaOneTimePasswordRepository jpaOneTimePasswordRepository;

    @Override
    public OneTimePasswordTransaction saveAndFlush(OneTimePasswordTransaction oneTimePasswordTransaction) {
        return jpaOneTimePasswordRepository.saveAndFlush(OneTimePasswordTransactionEntity.fromModel(oneTimePasswordTransaction)).toModel();
    }

    @Override
    public OneTimePasswordTransaction save(OneTimePasswordTransaction oneTimePasswordTransaction) {
        return jpaOneTimePasswordRepository.save(OneTimePasswordTransactionEntity.fromModel(oneTimePasswordTransaction)).toModel();
    }

    @Override
    public Optional<OneTimePasswordTransaction> retrieveById(UUID id) {
        return jpaOneTimePasswordRepository.findById(id)
                .map(OneTimePasswordTransactionEntity::toModel);
    }

    @Override
    public Optional<OneTimePasswordTransaction> retrieveByRecipient(String recipient) {
        return jpaOneTimePasswordRepository.findByRecipientAndStatus(recipient, OtpStatus.VERIFIED)
                .map(OneTimePasswordTransactionEntity::toModel);
    }

    @Override
    public Optional<OneTimePasswordTransaction> retrieveByRecipientAndStatus(String recipient, List<OtpStatus> otpStatuses) {
        return jpaOneTimePasswordRepository.findByRecipientAndStatusIn(recipient, otpStatuses)
                .map(OneTimePasswordTransactionEntity::toModel);
    }
}