package com.fabiankevin.app.config;

import com.fabiankevin.app.clients.EmailOtpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@Configuration
public class EmailConfig {

    @Bean
    public EmailOtpClient emailOtpClient(JavaMailSender javaMailSender, TemplateEngine templateEngine){
        return new EmailOtpClient(javaMailSender, templateEngine);
    }
}
