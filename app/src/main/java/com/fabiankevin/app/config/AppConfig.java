package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.AutotestEmailNotificationClient;
import com.fabiankevin.app.clients.EmailNotificationClient;
import com.fabiankevin.app.models.enums.DeliveryMethod;
import com.fabiankevin.app.persistence.OtpTransactionRepository;
import com.fabiankevin.app.properties.OtpProperties;
import com.fabiankevin.app.services.otp.AutotestOneTimePasswordService;
import com.fabiankevin.app.services.otp.DefaultOneTimePasswordGenerator;
import com.fabiankevin.app.services.otp.DefaultOneTimePasswordService;
import com.fabiankevin.app.services.otp.OneTimePasswordService;
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
    public OneTimePasswordService defaultOtpService(OtpTransactionRepository otpTransactionRepository,
                                                    EmailNotificationClient emailOtpClient,
                                                    OtpProperties otpProperties) {
        return new DefaultOneTimePasswordService(otpTransactionRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient
                ),
                new DefaultOneTimePasswordGenerator(),
                otpProperties);
    }

    @Bean
    @Profile("autotest")
    public OneTimePasswordService autotestOtpService(OtpTransactionRepository otpTransactionRepository,
                                                     AutotestEmailNotificationClient emailOtpClient,
                                                     OtpProperties otpProperties) {
        return new AutotestOneTimePasswordService(otpTransactionRepository,
                Map.of(
                        DeliveryMethod.EMAIL, emailOtpClient
                ),
                new DefaultOneTimePasswordGenerator(),
                otpProperties);
    }
}
