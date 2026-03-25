package com.jpsoftware.farmapp.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI farmAppOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FarmApp API")
                        .version("v1")
                        .description("REST API for farm management operations, including animals, feeding, production, users, feed types, and dashboard metrics.")
                        .contact(new Contact().name("FarmApp Team")));
    }
}
