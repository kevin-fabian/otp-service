package com.fabiankevin.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fabiankevin.microwebspringbootstarter.web.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ResourceServerConfig {
    private final InvalidJwtAuthenticationEntryPoint invalidJwtAuthenticationEntryPoint;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/v1/otp/*").hasAnyAuthority("SCOPE_otp:read", "SCOPE_otp:manage", "ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/otp/**").hasAnyAuthority("SCOPE_otp:manage", "SCOPE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/totp/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/totp/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(Customizer.withDefaults())
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    ApiErrorResponse errorResponse = new ApiErrorResponse();
                                    errorResponse.setMessage("Forbidden");
                                    errorResponse.setDetails("Insufficient permissions to access this resource");
                                    errorResponse.setTimestamp(Instant.now());
                                    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                                })
                                .authenticationEntryPoint(invalidJwtAuthenticationEntryPoint));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
