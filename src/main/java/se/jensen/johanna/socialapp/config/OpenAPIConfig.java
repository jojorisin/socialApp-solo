package se.jensen.johanna.socialapp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

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
