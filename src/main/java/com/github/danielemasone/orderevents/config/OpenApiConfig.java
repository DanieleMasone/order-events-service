package com.github.danielemasone.orderevents.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI metadata configuration shared by runtime Swagger UI, the build-time spec export,
 * and the generated static API documentation.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Adds project-level API metadata that appears in runtime and static API documentation.
     *
     * @return OpenAPI metadata model
     */
    @Bean
    public OpenAPI orderEventsOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server")))
                .info(new Info()
                        .title("Order Events Service API")
                        .version("0.1.0")
                        .description("REST API for creating orders and publishing order-created Kafka events."));
    }
}
