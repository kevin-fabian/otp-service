package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.OtpTransaction;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.entities.OtpTransactionEntity;
import com.fabiankevin.app.persistence.jpa.JpaOtpRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
class DefaultOtpTransactionTransactionRepositoryTest {
    @Autowired
    private OtpTransactionRepository otpTransactionRepository;
    @Autowired
    private JpaOtpRepository jpaOtpRepository;

    private OtpTransaction mockedOtpTransaction;

    @TestConfiguration
    static class BeanConfiguration {
        @Bean
        public OtpTransactionRepository defaultOtpRepository(JpaOtpRepository jpaOtpRepository){
            return new DefaultOtpTransactionRepository(jpaOtpRepository);
        }
    }

    @BeforeEach
    void setup(){
        OffsetDateTime now = OffsetDateTime.now();
        mockedOtpTransaction = OtpTransaction.builder()
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.SMS)
                .recipient("test@test.com")
                .status(OtpStatus.ACTIVE)
                .metadata("{}")
                .attemptCount(0)
                .createdAt(now)
                .expiresAt(now.plusMinutes(1))
                .otpCode("123456")
                .build();
    }

    @Test
    void saveAndFlush_givenValidOtp_thenShouldSave() {
        OtpTransaction savedOtpTransaction = otpTransactionRepository.saveAndFlush(mockedOtpTransaction);

        OtpTransactionEntity otpTransactionEntity = jpaOtpRepository.findById(savedOtpTransaction.id()).get();

        Assertions.assertThat(savedOtpTransaction)
                .usingRecursiveComparison()
                .isEqualTo(otpTransactionEntity.toModel());
    }

    @Test
    void saveAndFlush_givenNullOtp_thenShouldThrowException() {
        Assertions.assertThatThrownBy(() -> otpTransactionRepository.saveAndFlush(null))
                .isInstanceOf(NullPointerException.class)
                .describedAs("Expecting null pointer exception");
    }

    @Test
    void retrieveById_givenExistingId_thenShouldReturnOtp() {
        OtpTransaction savedOtpTransaction = otpTransactionRepository.saveAndFlush(mockedOtpTransaction);

        Optional<OtpTransaction> retrievedOtp = otpTransactionRepository.retrieveById(savedOtpTransaction.id());

        Assertions.assertThat(retrievedOtp).isPresent();
        Assertions.assertThat(retrievedOtp.get())
                .usingRecursiveComparison()
                .isEqualTo(savedOtpTransaction);
    }

    @Test
    void retrieveById_givenNonExistingId_thenShouldReturnEmpty() {
        Optional<OtpTransaction> retrievedOtp = otpTransactionRepository.retrieveById(UUID.randomUUID());

        Assertions.assertThat(retrievedOtp).isNotPresent();
    }

    @Test
    void retrieveRecipientAndActiveStatusAndNotExpired_givenActiveAndNotExpiredOtp_thenShouldReturnOtp() {
        OtpTransaction savedOtpTransaction = otpTransactionRepository.saveAndFlush(mockedOtpTransaction);

        Optional<OtpTransaction> retrievedOtp = otpTransactionRepository.retrieveRecipientAndActiveStatusAndNotExpired(mockedOtpTransaction.recipient());

        Assertions.assertThat(retrievedOtp).isPresent();
        Assertions.assertThat(retrievedOtp.get())
                .usingRecursiveComparison()
                .isEqualTo(savedOtpTransaction);
    }

    @Test
    void retrieveRecipientAndActiveStatusAndNotExpired_givenExpiredOtp_thenShouldReturnEmpty() {
        mockedOtpTransaction = mockedOtpTransaction.toBuilder()
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        otpTransactionRepository.saveAndFlush(mockedOtpTransaction);

        Optional<OtpTransaction> retrievedOtp = otpTransactionRepository.retrieveRecipientAndActiveStatusAndNotExpired(mockedOtpTransaction.recipient());

        Assertions.assertThat(retrievedOtp).isEmpty();
    }

    @Test
    void retrieveRecipient_thenShouldReturnEmpty() {
        Optional<OtpTransaction> retrievedOtp = otpTransactionRepository.retrieveRecipientAndActiveStatusAndNotExpired("nonexistent@test.com");

        Assertions.assertThat(retrievedOtp).isEmpty();
    }

    @Test
    void save_givenValidOtp_thenShouldSaveAndReturnSavedEntity() {
        OtpTransaction savedOtpTransaction = otpTransactionRepository.save(mockedOtpTransaction);

        OtpTransactionEntity otpTransactionEntity = jpaOtpRepository.findById(savedOtpTransaction.id()).get();
        Assertions.assertThat(otpTransactionEntity.toModel())
                .usingRecursiveComparison()
                .isEqualTo(savedOtpTransaction);
    }

    @Test
    void save_givenNullOtp_thenShouldThrowException() {
        Assertions.assertThatThrownBy(() -> otpTransactionRepository.save(null))
                .isInstanceOf(NullPointerException.class)
                .describedAs("Expecting NullPointerException when saving null OTP");
    }
}