package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.Otp;
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
        @Index(name = "otps_user_identifier_idx", columnList = "user_identifier")
})
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "otp_code", nullable = false)
    private String otpCode;

    @Column(name = "user_identifier", nullable = false)
    private String userIdentifier;

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

    @Column(name = "last_attempt_at")
    private OffsetDateTime lastAttemptAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private DeliveryMethod deliveryMethod;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "metadata")
    private String metadata;

    public Otp toModel() {
        return Otp.builder()
                .id(id)
                .otpCode(otpCode)
                .userIdentifier(userIdentifier)
                .purpose(purpose)
                .status(status)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .lastAttemptAt(lastAttemptAt)
                .deliveryMethod(deliveryMethod)
                .attemptCount(attemptCount)
                .metadata(metadata)
                .build();
    }

    public static OtpEntity fromModel(Otp otp) {
        OtpEntity entity = new OtpEntity();
        entity.setId(otp.id());
        entity.setOtpCode(otp.otpCode());
        entity.setUserIdentifier(otp.userIdentifier());
        entity.setPurpose(otp.purpose());
        entity.setStatus(otp.status());
        entity.setCreatedAt(otp.createdAt());
        entity.setExpiresAt(otp.expiresAt());
        entity.setLastAttemptAt(otp.lastAttemptAt());
        entity.setDeliveryMethod(otp.deliveryMethod());
        entity.setAttemptCount(otp.attemptCount());
        entity.setMetadata(otp.metadata());
        return entity;
    }
}