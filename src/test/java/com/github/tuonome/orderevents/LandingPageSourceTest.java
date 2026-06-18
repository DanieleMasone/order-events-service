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
                .contains("href=\"./docs/user-guide.md\"")
                .contains("href=\"./jacoco/index.html\"")
                .contains("href=\"./openapi/\"")
                .contains("href=\"./openapi/openapi.json\"")
                .contains("https://github.com/danielemasone/order-events-service")
                .contains("Operational commands, API examples, event flow, and troubleshooting")
                .contains("Intentional Scope")
                .contains("Deliberate exclusions")
                .contains("localStorage")
                .contains("prefers-reduced-motion");

        assertThat(html).doesNotContain("href=\"http://localhost:8080/swagger-ui.html\"");
        assertThat(html).doesNotContain("href=\"http://localhost:8080/actuator/health\"");
        assertThat(html).doesNotContain("Future Improvements");
    }
}
