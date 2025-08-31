package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.OtpTransaction;

import java.util.Optional;
import java.util.UUID;

public interface OtpTransactionRepository {
    OtpTransaction saveAndFlush(OtpTransaction otpTransaction);
    OtpTransaction save(OtpTransaction otpTransaction);
    Optional<OtpTransaction> retrieveById(UUID id);
    Optional<OtpTransaction> retrieveRecipientAndActiveStatusAndNotExpired(String recipient);
}