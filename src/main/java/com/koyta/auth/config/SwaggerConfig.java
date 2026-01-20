package com.koyta.auth.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auth Application build by Abhishek Narkhede.",
                description = "Generic auth app that can be used with any application.",
                contact = @Contact(
                        name = "Abhishek Sanjay Narkhede",
                        url = "https://www.koytaindustries.com/",
                        email = "support@koytaindustries.com"
                ),
                version = "1.0",
                summary = "This app is very useful if you dont want create auth app from scratch."

        )
        ,
        security = {
                @SecurityRequirement(
                        name="bearerAuth"
                )
        }
)
//@SecurityScheme(
//        name = "bearerAuth",
//        type = SecuritySchemeType.HTTP,
//        scheme = "bearer", //Authorization: Bearer htokenaswga,
//        bearerFormat = "JWT"
//
//)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        OpenAPI openAPI = new OpenAPI();


        io.swagger.v3.oas.models.info.Info info = new io.swagger.v3.oas.models.info.Info();
        info.setTitle("Authetication API's");
        info.setDescription("Auth Api's");
        info.setVersion("1.0.0");

        io.swagger.v3.oas.models.security.SecurityScheme securityScheme = new io.swagger.v3.oas.models.security.SecurityScheme();

        securityScheme.name("Authorization")
                .scheme("bearer")
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER);

        Components components = new Components().addSecuritySchemes("Token", securityScheme);

        openAPI.setSecurity(List.of(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("Token")));
        openAPI.setComponents(components);

        return openAPI;
    }

}
