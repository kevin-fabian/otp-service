package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.AutotestEmailOtpClient;
import com.fabiankevin.app.clients.EmailOtpClient;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.otp.AutotestOtpService;
import com.fabiankevin.app.services.otp.DefaultOtpGenerator;
import com.fabiankevin.app.services.otp.DefaultOtpService;
import com.fabiankevin.app.services.otp.OtpService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Configuration
public class AppConfig {
    @ConfigurationProperties("otp")
    @Bean
    public OtpProperties otpProperties() {
        return new OtpProperties();
    }

    @Bean
    @Profile("!autotest")
    public OtpService defaultOtpService(OtpTransactionRepository otpTransactionRepository,
                                        EmailOtpClient emailOtpClient,
                                        OtpProperties otpProperties) {
        return new DefaultOtpService(otpTransactionRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }

    @Bean
    @Profile("autotest")
    public OtpService autotestOtpService(OtpTransactionRepository otpTransactionRepository,
                                      AutotestEmailOtpClient emailOtpClient,
                                      OtpProperties otpProperties) {
        return new AutotestOtpService(otpTransactionRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }
}
