package com.fabiankevin.app.persistence.entities;

import com.fabiankevin.app.models.TotpUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "totp_users",  indexes = {
        @Index(name = "totp_users_user_id_uidx", columnList = "userId", unique = true)
})
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TotpUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String userId;
    private String secret;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    public static TotpUserEntity from(TotpUser totpUser) {
        return TotpUserEntity.builder()
                .id(totpUser.id())
                .userId(totpUser.userReferenceId())
                .secret(totpUser.secret())
                .createdAt(totpUser.createdAt())
                .updatedAt(totpUser.updatedAt())
                .build();
    }

    public TotpUser toModel() {
        return new TotpUser(
                this.id,
                this.userId,
                this.secret,
                this.createdAt,
                this.updatedAt
        );
    }

}
