package com.fabiankevin.app.persistence.entities;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity(name = "otps")
@Table(name = "otps", indexes = {
        @Index(name = "otps_otp_code_idx", columnList = "otp_code"),
        @Index(name = "otps_recipient_idx", columnList = "recipient")
})
public class OtpTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private OtpPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OtpStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private DeliveryMethod deliveryMethod;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "metadata")
    private String metadata;

    public OtpTransaction toModel() {
        return OtpTransaction.builder()
                .id(id)
                .otpCode(otpCode)
                .recipient(recipient)
                .purpose(purpose)
                .status(status)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .updatedAt(updatedAt)
                .deliveryMethod(deliveryMethod)
                .attemptCount(attemptCount)
                .metadata(metadata)
                .build();
    }

    public static OtpTransactionEntity fromModel(OtpTransaction otpTransaction) {
        OtpTransactionEntity entity = new OtpTransactionEntity();
        entity.setId(otpTransaction.id());
        entity.setOtpCode(otpTransaction.otpCode());
        entity.setRecipient(otpTransaction.recipient());
        entity.setPurpose(otpTransaction.purpose());
        entity.setStatus(otpTransaction.status());
        entity.setCreatedAt(otpTransaction.createdAt());
        entity.setExpiresAt(otpTransaction.expiresAt());
        entity.setUpdatedAt(otpTransaction.updatedAt());
        entity.setDeliveryMethod(otpTransaction.deliveryMethod());
        entity.setAttemptCount(otpTransaction.attemptCount());
        entity.setMetadata(otpTransaction.metadata());
        return entity;
    }
}