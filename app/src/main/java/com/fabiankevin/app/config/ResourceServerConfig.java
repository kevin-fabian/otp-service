package com.fabiankevin.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ResourceServerConfig {
    private final JsonMapper jsonMapper;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            AuthenticationEntryPoint defaultInvalidTokenAuthenticationEntryPoint,
            AccessDeniedHandler defaultInvalidTokenAccessDeniedHandler) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/v1/otps/**").hasAnyAuthority("SCOPE_otp:read", "SCOPE_otp:manage", "ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/otps/**").hasAnyAuthority("SCOPE_otp:manage", "SCOPE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/totp/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "SCOPE_totp:manage")
                        .requestMatchers(HttpMethod.POST, "/v1/totp/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN", "SCOPE_totp:manage")
                        .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(
                        oauth2 -> oauth2
                                .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter))
                                .accessDeniedHandler(defaultInvalidTokenAccessDeniedHandler)
                                .authenticationEntryPoint(defaultInvalidTokenAuthenticationEntryPoint));
        return http.build();
    }

    @Bean
    public RoleHierarchyImpl roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<String> scopes = List.of(jwt.getClaimAsString("scope").split(" "));
            if (scopes.isEmpty()) {
                scopes = jwt.getClaimAsStringList("scp");
            }

            List<GrantedAuthority> authorities = new ArrayList<>(scopes.stream()
                    .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                    .toList());

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                authorities.addAll(roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList());
            }

            return authorities;
        });
        return converter;
    }
}
