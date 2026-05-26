package com.github.tuonome.orderevents;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LandingPageSourceTest {

    @Test
    void landingPageSourceContainsExpectedDocumentationLinks() throws Exception {
        String html = Files.readString(Path.of("src", "site", "index.html"));

        assertThat(html)
                .contains("href=\"./jacoco/index.html\"")
                .contains("href=\"./openapi/\"")
                .contains("href=\"./openapi/openapi.json\"")
                .contains("href=\"http://localhost:8080/actuator/health\"")
                .contains("API documentation is generated from the OpenAPI JSON")
                .contains("localStorage")
                .contains("prefers-reduced-motion");

        assertThat(html).doesNotContain("href=\"http://localhost:8080/swagger-ui.html\"");
    }
}
