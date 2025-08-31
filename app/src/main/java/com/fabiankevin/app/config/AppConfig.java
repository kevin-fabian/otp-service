package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.EmailOtpClient;
import com.fabiankevin.app.clients.LocalEmailOtpClient;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.otp.DefaultOtpGenerator;
import com.fabiankevin.app.services.otp.DefaultOtpService;
import com.fabiankevin.app.services.otp.LocalOtpService;
import com.fabiankevin.app.services.otp.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Map;

@Configuration
public class AppConfig {
    @Autowired
    private Environment environment;

    @ConfigurationProperties("otp")
    @Bean
    public OtpProperties otpProperties() {
        return new OtpProperties();
    }

    @Bean
    @Profile("!local-h2")
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
    @Profile("local-h2")
    @Primary
    public OtpService localOtpService(OtpTransactionRepository otpTransactionRepository,
                                      LocalEmailOtpClient emailOtpClient,
                                      OtpProperties otpProperties) {
        return new LocalOtpService(otpTransactionRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }
}
