package com.fabiankevin.app.persistence.entities;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "otp_transactions", indexes = {
        @Index(name = "otp_transactions_otp_code_idx", columnList = "otp_code"),
        @Index(name = "otp_transactions_recipient_idx", columnList = "recipient")
})
public class OneTimePasswordTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "otp_code", columnDefinition = "VARCHAR(8)", nullable = false)
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
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private DeliveryMethod deliveryMethod;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "metadata")
    private String metadata;

    public OneTimePasswordTransaction toModel() {
        return OneTimePasswordTransaction.builder()
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

    public static OneTimePasswordTransactionEntity fromModel(OneTimePasswordTransaction oneTimePasswordTransaction) {
        OneTimePasswordTransactionEntity entity = new OneTimePasswordTransactionEntity();
        entity.setId(oneTimePasswordTransaction.id());
        entity.setOtpCode(oneTimePasswordTransaction.otpCode());
        entity.setRecipient(oneTimePasswordTransaction.recipient());
        entity.setPurpose(oneTimePasswordTransaction.purpose());
        entity.setStatus(oneTimePasswordTransaction.status());
        entity.setCreatedAt(oneTimePasswordTransaction.createdAt());
        entity.setExpiresAt(oneTimePasswordTransaction.expiresAt());
        entity.setUpdatedAt(oneTimePasswordTransaction.updatedAt());
        entity.setDeliveryMethod(oneTimePasswordTransaction.deliveryMethod());
        entity.setAttemptCount(oneTimePasswordTransaction.attemptCount());
        entity.setMetadata(oneTimePasswordTransaction.metadata());
        return entity;
    }
}