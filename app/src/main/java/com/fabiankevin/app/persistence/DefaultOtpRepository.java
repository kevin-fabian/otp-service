package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.jpa.JpaOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DefaultOtpRepository implements OtpRepository {
    private final JpaOtpRepository jpaOtpRepository;

    @Override
    public Otp saveAndFlush(Otp otp) {
        OtpEntity entity = OtpEntity.fromModel(otp);
        return jpaOtpRepository.save(entity).toModel();
    }

    @Override
    public Optional<Otp> retrieveById(UUID id) {
        return jpaOtpRepository.findById(id)
                .map(OtpEntity::toModel);
    }

    @Override
    public Optional<Otp> retrieveByUserIdentifierAndActiveStatusAndNotExpired(String userIdentifier) {
        return jpaOtpRepository.findByUserIdentifierAndStatusAndExpiresAtGreaterThan(userIdentifier,
                OtpStatus.ACTIVE,
                OffsetDateTime.now())
                .map(OtpEntity::toModel);
    }
}