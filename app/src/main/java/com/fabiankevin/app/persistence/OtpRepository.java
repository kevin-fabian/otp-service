package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.Otp;

import java.util.Optional;
import java.util.UUID;

public interface OtpRepository {
    Otp saveAndFlush(Otp otp);
    Optional<Otp> retrieveById(UUID id);
    boolean existByUserIdentifierAndStatusActive(String userIdentifier);
    Optional<Otp> retrieveByUserIdentifierAndActiveStatusAndNotExpired(String userIdentifier);
}