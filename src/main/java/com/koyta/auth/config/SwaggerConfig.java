package com.koyta.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        io.swagger.v3.oas.models.info.Info info =
                new io.swagger.v3.oas.models.info.Info()
                        .title("Authentication Service")
                        .description("Generic authentication & authorization service with JWT and OAuth2")
                        .version("1.0.0")
                        .summary("Reusable auth service for any application")
                        .contact(new io.swagger.v3.oas.models.info.Contact()
                                .name("Abhishek Sanjay Narkhede")
                                .email("support@koytaindustries.com")
                                .url("https://www.koytaindustries.com")
                        );

        // -------- Security Scheme --------
        io.swagger.v3.oas.models.security.SecurityScheme securityScheme =
                new io.swagger.v3.oas.models.security.SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER);

        // -------- Components --------
        Components components = new Components()
                .addSecuritySchemes("Token", securityScheme);

        // -------- OpenAPI --------
        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .components(components)
                .addSecurityItem(
                        new io.swagger.v3.oas.models.security.SecurityRequirement()
                                .addList("Token")
                );

        return openAPI;
    }
}
