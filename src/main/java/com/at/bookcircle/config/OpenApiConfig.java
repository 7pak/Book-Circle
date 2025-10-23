package com.at.bookcircle.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Abood",
                        email = "abood@gmail.com",
                        url = "https://abood.com"
                ),
                description = "OpenApi doc for spring security",
                title = "openApi specification - abood",
                version = "1.0",
                license = @License(
                        name = "License name",
                        url = "https://someurl.com"
                ),
                termsOfService = "Terms of service "
        ),
        servers = {
                @Server(
                        description = "Local ENV",
                        url = "https://localhost:8088/api/v1"
                ),
                @Server(
                         description = "PROD ENV",
                        url = "https://abood.com"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth desc",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
