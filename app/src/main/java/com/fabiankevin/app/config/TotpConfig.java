package com.fabiankevin.app.config;

import com.fabiankevin.app.properties.TotpProperties;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TotpConfig {

    @Bean
    public CodeVerifier codeVerifier(){
        TimeProvider timeProvider = new SystemTimeProvider();

        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        DefaultCodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        codeVerifier.setTimePeriod(30);
        codeVerifier.setAllowedTimePeriodDiscrepancy(0);
        return codeVerifier;
    }

    @Bean
    public QrGenerator zxingQrGenerator() {
        return new ZxingPngQrGenerator();
    }


    @Bean
    @ConfigurationProperties("totp")
    public TotpProperties totpProperties() {
        return new TotpProperties();
    }
}
