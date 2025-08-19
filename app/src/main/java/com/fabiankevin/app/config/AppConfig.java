package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.EmailOtpClient;
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

    @Bean("!local-h2")
    public OtpService defaultOtpService(OtpRepository otpRepository,
                                        EmailOtpClient emailOtpClient,
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
    public OtpService localOtpService(OtpRepository otpRepository,
                                      LocalEmailOtpClient emailOtpClient,
                                      OtpProperties otpProperties) {
        return new LocalOtpService(otpRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }
}
