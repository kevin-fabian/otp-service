package com.fabiankevin.app.config;

import com.fabiankevin.app.properties.OtpProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @ConfigurationProperties("otp")
    @Bean
    public OtpProperties otpProperties(){
        return new OtpProperties();
    }
}
