package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.TotpUser;
import com.fabiankevin.app.persistence.jpa.JpaTotpUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class DefaultTotpUserRepositoryTest {
    @Autowired
    private TotpUserRepository totpUserRepository;

    private TotpUser totpUser;

    @TestConfiguration
    static class ContextConfiguration {
        @Bean
        public TotpUserRepository defaultTotpUserRepository(JpaTotpUserRepository jpaTotpUserRepository) {
            return new DefaultTotpUserRepository(jpaTotpUserRepository);
        }
    }

    @BeforeEach
    void setup() {
        totpUser = TotpUser.builder()
                .userReferenceId("user-123")
                .secret("secret-key")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void save_givenValidTotpUser_thenShouldSaveSuccessfully() {
        TotpUser savedTotpUser = totpUserRepository.save(totpUser);

        assertNotNull(savedTotpUser.id(), "Id should not be null");
        assertEquals(totpUser.userReferenceId(), savedTotpUser.userReferenceId(), "User reference IDs should match");
        assertEquals(totpUser.secret(), savedTotpUser.secret(), "Secrets should match");
        assertEquals(totpUser.createdAt(), savedTotpUser.createdAt(), "CreatedAt should match");
        assertEquals(totpUser.updatedAt(), savedTotpUser.updatedAt(), "UpdatedAt should match");
    }

    @Test
    void save_givenNullTotpUser_thenShouldThrowException() {
        assertThrows(NullPointerException.class, () -> totpUserRepository.save(null),
                "Should throw NullPointerException when saving null TOTP user");
    }

    @Test
    void findByUserReferenceId_givenValidReferenceId_thenShouldReturnTotpUser() {
        totpUserRepository.save(totpUser);

        Optional<TotpUser> foundUser = totpUserRepository.findByUserReferenceId(totpUser.userReferenceId());

        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals(totpUser.userReferenceId(), foundUser.get().userReferenceId(), "User reference IDs should match");
        assertEquals(totpUser.secret(), foundUser.get().secret(), "Secrets should match");
        assertEquals(totpUser.createdAt(), foundUser.get().createdAt(), "CreatedAt should match");
        assertEquals(totpUser.updatedAt(), foundUser.get().updatedAt(), "UpdatedAt should match");

    }

    @Test
    void findByUserReferenceId_givenNonExistingReferenceId_thenShouldReturnEmpty() {
        Optional<TotpUser> foundUser = totpUserRepository.findByUserReferenceId("non-existent");

        assertTrue(foundUser.isEmpty(), "User should not be found");
    }

    @Test
    void findById_givenValidId_thenShouldReturnTotpUser() {
        TotpUser savedTotpUser = totpUserRepository.save(totpUser);

        Optional<TotpUser> foundUser = totpUserRepository.findById(savedTotpUser.id());

        assertTrue(foundUser.isPresent(), "User should be found");
        assertEquals(savedTotpUser.id(), foundUser.get().id(), "Ids should match");
        assertEquals(savedTotpUser.userReferenceId(), foundUser.get().userReferenceId(), "User reference IDs should match");
        assertEquals(savedTotpUser.secret(), foundUser.get().secret(), "Secrets should match");
        assertEquals(savedTotpUser.createdAt(), foundUser.get().createdAt(), "CreatedAt should match");
        assertEquals(savedTotpUser.updatedAt(), foundUser.get().updatedAt(), "UpdatedAt should match");
    }

    @Test
    void findById_givenInvalidId_thenShouldReturnEmpty() {
        Optional<TotpUser> foundUser = totpUserRepository.findById(UUID.randomUUID());

        assertTrue(foundUser.isEmpty(), "User should not be found");
    }

    @Test
    void findById_givenNullId_thenShouldThrowException() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> totpUserRepository.findById(null),
                "Should throw InvalidDataAccessApiUsageException when ID is null");
    }
}