package com.fabiankevin.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(title = "${spring.application.name}",
                version = "v1",
                description = """
                        The OTP service is responsible for handling various types of OTP (One-Time Password) generation and verification, including TOTP (Time-based One-Time Password) and SMS-based OTPs.
                        It provides secure authentication mechanisms for users through different channels.
                        The service supports integration with external systems for sending OTPs via SMS and offers endpoints for managing OTP-related operations.
                        """)
)
@SecurityScheme(
        name = "Spring Oauth2",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(
                clientCredentials = @OAuthFlow(
                        tokenUrl = "http://localhost:9000/oauth2/token",
                        scopes = {
                                @io.swagger.v3.oas.annotations.security.OAuthScope(name = "otp:manage", description = "Read and write access to OTP management"),
                        }
                )
        )
)
public class OpenApiConfig {
}