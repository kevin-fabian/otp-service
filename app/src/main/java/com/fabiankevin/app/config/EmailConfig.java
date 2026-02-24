package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.EmailNotificationClient;
import com.fabiankevin.app.properties.OtpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import java.util.concurrent.Executor;

@Configuration
public class EmailConfig {

    @Bean
    @Profile("!autotest") // Exclude this bean in local profile
    public EmailNotificationClient emailOtpClient(JavaMailSender javaMailSender, TemplateEngine templateEngine,
                                                  OtpProperties otpProperties, Executor virtualThreadExecutor){
        return new EmailNotificationClient(javaMailSender, templateEngine, "OTP", otpProperties.getExpirationMinutes(), virtualThreadExecutor);
    }
}
