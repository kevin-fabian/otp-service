package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.OneTimePasswordTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.entities.OneTimePasswordTransactionEntity;
import com.fabiankevin.app.persistence.jpa.JpaOneTimePasswordRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
class DefaultOneTimePasswordTransactionRepositoryTest {
    @Autowired
    private OtpTransactionRepository otpTransactionRepository;
    @Autowired
    private JpaOneTimePasswordRepository jpaOneTimePasswordRepository;

    private OneTimePasswordTransaction mockedOneTimePasswordTransaction;

    @TestConfiguration
    static class BeanConfiguration {
        @Bean
        public OtpTransactionRepository defaultOtpRepository(JpaOneTimePasswordRepository jpaOneTimePasswordRepository) {
            return new DefaultOtpTransactionRepository(jpaOneTimePasswordRepository);
        }
    }

    @BeforeEach
    void setup() {
        OffsetDateTime now = OffsetDateTime.now();
        mockedOneTimePasswordTransaction = OneTimePasswordTransaction.builder()
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.SMS)
                .recipient("test@test.com")
                .status(OtpStatus.NEW)
                .metadata("{}")
                .attemptCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .expiresAt(now.plusMinutes(1))
                .otpCode("123456")
                .build();
    }

    @Test
    void saveAndFlush_givenValidOtp_thenShouldSave() {
        OneTimePasswordTransaction savedOneTimePasswordTransaction = otpTransactionRepository.saveAndFlush(mockedOneTimePasswordTransaction);

        OneTimePasswordTransactionEntity oneTimePasswordTransactionEntity = jpaOneTimePasswordRepository.findById(savedOneTimePasswordTransaction.id()).get();

        Assertions.assertThat(savedOneTimePasswordTransaction)
                .usingRecursiveComparison()
                .isEqualTo(oneTimePasswordTransactionEntity.toModel());
    }

    @Test
    void saveAndFlush_givenNullOtp_thenShouldThrowException() {
        Assertions.assertThatThrownBy(() -> otpTransactionRepository.saveAndFlush(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void retrieveById_givenExistingId_thenShouldReturnOtp() {
        OneTimePasswordTransaction savedOneTimePasswordTransaction = otpTransactionRepository.saveAndFlush(mockedOneTimePasswordTransaction);

        Optional<OneTimePasswordTransaction> retrievedOtp = otpTransactionRepository.retrieveById(savedOneTimePasswordTransaction.id());

        Assertions.assertThat(retrievedOtp).isPresent();
        Assertions.assertThat(retrievedOtp.get())
                .usingRecursiveComparison()
                .isEqualTo(savedOneTimePasswordTransaction);
    }

    @Test
    void retrieveById_givenNonExistingId_thenShouldReturnEmpty() {
        Optional<OneTimePasswordTransaction> retrievedOtp = otpTransactionRepository.retrieveById(UUID.randomUUID());

        Assertions.assertThat(retrievedOtp).isNotPresent();
    }

    @Test
    void retrieveByRecipient_givenExpiredOtp_thenShouldReturnEmpty() {
        mockedOneTimePasswordTransaction = mockedOneTimePasswordTransaction.toBuilder()
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        otpTransactionRepository.saveAndFlush(mockedOneTimePasswordTransaction);

        Optional<OneTimePasswordTransaction> retrievedOtp = otpTransactionRepository.retrieveByRecipient(mockedOneTimePasswordTransaction.recipient());

        Assertions.assertThat(retrievedOtp).isEmpty();
    }

    @Test
    void retrieveRecipient_thenShouldReturnEmpty() {
        Optional<OneTimePasswordTransaction> retrievedOtp = otpTransactionRepository.retrieveByRecipient("nonexistent@test.com");

        Assertions.assertThat(retrievedOtp).isEmpty();
    }

    @Test
    void save_givenValidOtp_thenShouldSaveAndReturnSavedEntity() {
        OneTimePasswordTransaction savedOneTimePasswordTransaction = otpTransactionRepository.save(mockedOneTimePasswordTransaction);

        OneTimePasswordTransactionEntity oneTimePasswordTransactionEntity = jpaOneTimePasswordRepository.findById(savedOneTimePasswordTransaction.id()).get();
        Assertions.assertThat(oneTimePasswordTransactionEntity.toModel())
                .usingRecursiveComparison()
                .isEqualTo(savedOneTimePasswordTransaction);
    }

    @Test
    void save_givenNullOtp_thenShouldThrowException() {
        Assertions.assertThatThrownBy(() -> otpTransactionRepository.save(null))
                .isInstanceOf(NullPointerException.class)
                .describedAs("Expecting NullPointerException when saving null OTP");
    }

    @Test
    void retrieveByRecipientAndStatusInAndNotExpired_givenMatchingStatus_thenShouldReturnOtp() {
        OneTimePasswordTransaction savedOneTimePasswordTransaction = otpTransactionRepository.saveAndFlush(mockedOneTimePasswordTransaction);

        Optional<OneTimePasswordTransaction> retrievedOtp = otpTransactionRepository
                .retrieveByRecipientAndStatus(
                        mockedOneTimePasswordTransaction.recipient(),
                        List.of(OtpStatus.NEW)
                );

        Assertions.assertThat(retrievedOtp).isPresent();
        Assertions.assertThat(retrievedOtp.get())
                .usingRecursiveComparison()
                .isEqualTo(savedOneTimePasswordTransaction);
    }

    @Test
    void retrieveByRecipientAndStatusInAndNotExpired_givenNonMatchingStatusOrExpired_thenShouldReturnEmpty() {
        otpTransactionRepository.saveAndFlush(mockedOneTimePasswordTransaction.withStatus(OtpStatus.USED));

        Optional<OneTimePasswordTransaction> retrievedOtpStatus = otpTransactionRepository
                .retrieveByRecipientAndStatus(
                        mockedOneTimePasswordTransaction.recipient(),
                        List.of(OtpStatus.NEW, OtpStatus.SENT, OtpStatus.VERIFIED)
                );
        Assertions.assertThat(retrievedOtpStatus).isEmpty();
    }
}