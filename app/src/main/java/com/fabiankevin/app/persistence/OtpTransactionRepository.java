package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.OtpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpTransactionRepository {
    OneTimePasswordTransaction saveAndFlush(OneTimePasswordTransaction oneTimePasswordTransaction);
    OneTimePasswordTransaction save(OneTimePasswordTransaction oneTimePasswordTransaction);
    Optional<OneTimePasswordTransaction> retrieveById(UUID id);
    Optional<OneTimePasswordTransaction> retrieveByRecipient(String recipient);
    Optional<OneTimePasswordTransaction> retrieveByRecipientAndStatus(String recipient, List<OtpStatus> otpStatuses);
}