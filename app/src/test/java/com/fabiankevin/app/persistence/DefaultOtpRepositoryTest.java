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

import java.time.LocalDateTime;

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
        LocalDateTime now = LocalDateTime.now();
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

        Assertions.assertThat(mockedOtp)
                .describedAs("should be equal except by id")
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(otpEntity.toModel());
    }
}