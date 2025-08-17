package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.persistence.entities.TotpUserEntity;
import com.fabiankevin.app.persistence.jpa.JpaTotpUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DefaultTotpUserRepository implements TotpUserRepository {
    private final JpaTotpUserRepository jpaTotpUserRepository;

    @Override
    public TotpUser save(TotpUser totpUser) {
        return jpaTotpUserRepository.save(TotpUserEntity.from(totpUser)).toModel();
    }

    @Override
    public Optional<TotpUser> findByUserReferenceId(String userReferenceId) {
        return jpaTotpUserRepository.findByUserReferenceId(userReferenceId)
                .map(TotpUserEntity::toModel);
    }
}
