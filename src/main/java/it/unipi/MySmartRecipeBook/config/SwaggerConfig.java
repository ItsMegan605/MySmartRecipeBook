package it.unipi.MySmartRecipeBook.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger (OpenAPI) configuration class.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the custom OpenAPI documentation and sets up JWT Bearer authentication.
     * @return the configured OpenAPI object with security schemes applied
     */
    @Bean
        public OpenAPI customOpenAPI() {
            final String securitySchemeName = "bearerAuth";

            return new OpenAPI()
                    .info(new Info()
                            .title("API Documentation")
                            .description("API Documentation with JWT Authentication")
                            .version("1.0.0"))
                    .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                    .components(new io.swagger.v3.oas.models.Components()
                            .addSecuritySchemes(securitySchemeName,
                                    new SecurityScheme()
                                            .name(securitySchemeName)
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("bearer")
                                            .bearerFormat("JWT")));
        }
}



