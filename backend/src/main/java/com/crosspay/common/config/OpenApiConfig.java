package com.crosspay.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Cross Pay API",
                version = "1.0.0",
                description = "Cross Pay backend API for secure multi-currency wallet management, FX quotes, transfers, deposits, withdrawals, transaction history, and ledger-backed financial operations."
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Authenticate with a JWT obtained from the login endpoint."
)
public class OpenApiConfig {

    @Bean
    OpenAPI crossPayOpenApi() {
        return new OpenAPI();
    }
}
