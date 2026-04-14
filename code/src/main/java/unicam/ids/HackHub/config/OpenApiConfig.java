package unicam.ids.HackHub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hackHubOpenApi() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("HackHub API")
                        .version("1.0")
                        .description("""
                                API per la gestione della piattaforma HackHub.

                                Flusso consigliato in Swagger:
                                1. Usa /api/auth/supported-roles per vedere i ruoli ammessi.
                                2. Esegui /api/auth/register oppure /api/auth/login.
                                3. Copia il token restituito.
                                4. Clicca Authorize e incolla il token JWT senza prefisso aggiuntivo.
                                """)
                        .contact(new Contact().name("HackHub Team")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Inserisci qui il token JWT restituito dal login o dalla registrazione.")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
