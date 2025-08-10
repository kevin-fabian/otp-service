package com.fabiankevin.app.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(title = "${spring.application.name}", version = "v1", description = "API Documentation for the Otp Service")
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