package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.OtpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpTransactionRepository {
    OtpTransaction saveAndFlush(OtpTransaction otpTransaction);
    OtpTransaction save(OtpTransaction otpTransaction);
    Optional<OtpTransaction> retrieveById(UUID id);
    Optional<OtpTransaction> retrieveByRecipientAndActiveStatusAndNotExpired(String recipient);
    Optional<OtpTransaction> retrieveByRecipientAndStatusInAndNotExpired(String recipient, List<OtpStatus> otpStatuses);
}