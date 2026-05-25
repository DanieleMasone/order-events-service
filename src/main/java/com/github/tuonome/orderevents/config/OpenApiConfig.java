package com.github.tuonome.orderevents.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata configuration used by Swagger UI and by the build-time OpenAPI export test.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Adds project-level API metadata that appears in Swagger UI and the generated OpenAPI JSON.
     *
     * @return OpenAPI metadata model
     */
    @Bean
    public OpenAPI orderEventsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Events Service API")
                        .version("0.1.0")
                        .description("REST API for creating orders and publishing order-created Kafka events."));
    }
}
