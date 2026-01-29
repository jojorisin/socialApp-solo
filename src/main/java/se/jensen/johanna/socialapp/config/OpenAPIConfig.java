package se.jensen.johanna.socialapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;


/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * <p>
 * This class defines the general API information, server configurations,
 * and security requirements for the application. It specifically sets up
 * JWT-based Bearer authentication to be used globally across all secured endpoints
 * in the Swagger UI.
 * </p>
 */

@OpenAPIDefinition(
        info = @Info(title = "API med JWT"
                , version = "1.0"),
        servers = {
                @Server(url = "/", description = "Default Server URL")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth"
        ,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer"
        ,
        bearerFormat = "JWT"
)
public class OpenAPIConfig {
}
