package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.LocalEmailOtpClient;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.DefaultOtpGenerator;
import com.fabiankevin.app.services.DefaultOtpService;
import com.fabiankevin.app.services.LocalOtpService;
import com.fabiankevin.app.services.OtpService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    public OtpService defaultOtpService(OtpRepository otpRepository,
                                        LocalEmailOtpClient emailOtpClient,
                                        OtpProperties otpProperties) {
        return new DefaultOtpService(otpRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }

    @Bean
    @Profile("local-h2")
    @Primary
    public LocalOtpService localOtpService(OtpService defaultOtpService, OtpRepository otpRepository) {
        return new LocalOtpService(defaultOtpService, otpRepository);
    }
}
