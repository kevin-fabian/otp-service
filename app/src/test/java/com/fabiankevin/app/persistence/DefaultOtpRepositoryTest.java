package com.fabiankevin.app.persistence;

import com.fabiankevin.app.models.Otp;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.models.enums.OtpPurpose;
import com.fabiankevin.app.models.enums.OtpStatus;
import com.fabiankevin.app.persistence.jpa.JpaOtpRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
class DefaultOtpRepositoryTest {
    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private JpaOtpRepository jpaOtpRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private Otp mockedOtp;

    @TestConfiguration
    static class BeanConfiguration {
        @Bean
        public OtpRepository defaultOtpRepository(JpaOtpRepository jpaOtpRepository){
            return new DefaultOtpRepository(jpaOtpRepository);
        }
    }

    @BeforeEach
    public void setup(){
        OffsetDateTime now = OffsetDateTime.now();
        mockedOtp = Otp.builder()
                .purpose(OtpPurpose.LOGIN)
                .deliveryMethod(DeliveryMethod.SMS)
                .userIdentifier("test@test.com")
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
        Otp savedOtp = otpRepository.saveAndFlush(mockedOtp);

        OtpEntity otpEntity = jpaOtpRepository.findById(savedOtp.id()).get();

        Assertions.assertThat(savedOtp)
                .usingRecursiveComparison()
                .isEqualTo(otpEntity.toModel());
    }

    @Test
    void saveAndFlush_givenNullOtp_thenShouldThrowException() {
        Assertions.assertThatThrownBy(() -> otpRepository.saveAndFlush(null))
                .isInstanceOf(NullPointerException.class)
                .describedAs("Expecting null pointer exception");
    }

    @Test
    void retrieveById_givenExistingId_thenShouldReturnOtp() {
        Otp savedOtp = otpRepository.saveAndFlush(mockedOtp);

        Optional<Otp> retrievedOtp = otpRepository.retrieveById(savedOtp.id());

        Assertions.assertThat(retrievedOtp).isPresent();
        Assertions.assertThat(retrievedOtp.get())
                .usingRecursiveComparison()
                .isEqualTo(savedOtp);
    }

    @Test
    void retrieveById_givenNonExistingId_thenShouldReturnEmpty() {
        Optional<Otp> retrievedOtp = otpRepository.retrieveById(UUID.randomUUID());

        Assertions.assertThat(retrievedOtp).isNotPresent();
    }

    @Test
    void retrieveByUserIdentifierAndActiveStatusAndNotExpired_givenActiveAndNotExpiredOtp_thenShouldReturnOtp() {
        Otp savedOtp = otpRepository.saveAndFlush(mockedOtp);

        Optional<Otp> retrievedOtp = otpRepository.retrieveByUserIdentifierAndActiveStatusAndNotExpired(mockedOtp.userIdentifier());

        Assertions.assertThat(retrievedOtp).isPresent();
        Assertions.assertThat(retrievedOtp.get())
                .usingRecursiveComparison()
                .isEqualTo(savedOtp);
    }

    @Test
    void retrieveByUserIdentifierAndActiveStatusAndNotExpired_givenExpiredOtp_thenShouldReturnEmpty() {
        mockedOtp = mockedOtp.toBuilder()
                .expiresAt(OffsetDateTime.now().minusMinutes(1))
                .build();
        otpRepository.saveAndFlush(mockedOtp);

        Optional<Otp> retrievedOtp = otpRepository.retrieveByUserIdentifierAndActiveStatusAndNotExpired(mockedOtp.userIdentifier());

        Assertions.assertThat(retrievedOtp).isEmpty();
    }

    @Test
    void retrieveByUserIdentifierAndActiveStatusAndNotExpired_givenNonExistingUserIdentifier_thenShouldReturnEmpty() {
        Optional<Otp> retrievedOtp = otpRepository.retrieveByUserIdentifierAndActiveStatusAndNotExpired("nonexistent@test.com");

        Assertions.assertThat(retrievedOtp).isEmpty();
    }

}