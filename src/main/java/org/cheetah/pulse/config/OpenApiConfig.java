// src/main/java/com/setupnet/johndoe/config/OpenApiConfig.java
package org.cheetah.pulse.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  public static final String BEARER_SCHEME = "bearer-jwt";

  @Bean
  public OpenAPI api() {
    return new OpenAPI()
      .info(new Info()
        .title("JohnDoe Auth API")
        .version("v1")
        .description("Auth con OTP su SMS e JWT"))
      .components(new Components().addSecuritySchemes(
        BEARER_SCHEME,
        new SecurityScheme()
          .type(SecurityScheme.Type.HTTP)
          .scheme("bearer")
          .bearerFormat("JWT")
      ));
    // Nota: non imposto un SecurityRequirement globale, così /auth/** resta “senza lucchetto” in UI.
  }
}