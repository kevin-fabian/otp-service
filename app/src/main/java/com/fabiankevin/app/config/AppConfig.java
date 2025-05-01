package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.EmailOtpClient;
import com.fabiankevin.app.clients.TestSMSOtpClient;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.persistence.OtpRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.DefaultOtpGenerator;
import com.fabiankevin.app.services.DefaultOtpService;
import com.fabiankevin.app.services.OtpService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                        EmailOtpClient emailOtpClient,
                                        OtpProperties otpProperties) {
        return new DefaultOtpService(otpRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient,
                        DeliveryMethod.SMS, new TestSMSOtpClient()
                ),
                new DefaultOtpGenerator(),
                otpProperties);
    }
}
