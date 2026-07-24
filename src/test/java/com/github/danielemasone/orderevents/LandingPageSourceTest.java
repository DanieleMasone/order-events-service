package com.github.danielemasone.orderevents;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LandingPageSourceTest {

    @Test
    void landingPageSourceContainsExpectedDocumentationLinks() throws Exception {
        String html = Files.readString(Path.of("src", "site", "index.html"));

        assertThat(html)
                .contains("href=\"./docs/\"")
                .contains("href=\"./jacoco/index.html\"")
                .contains("href=\"./openapi/\"")
                .contains("href=\"./openapi/openapi.json\"")
                .contains("href=\"./assets/site.css\"")
                .contains("src=\"./assets/site.js\"")
                .contains("https://github.com/danielemasone/order-events-service")
                .contains("HTML guide generated from <code>docs/user-guide.md</code>")
                .contains("Intentional Scope")
                .contains("Deliberate exclusions")
                .contains("Atomic idempotency claim");

        assertThat(Files.readString(Path.of("src", "site", "assets", "site.js")))
                .contains("localStorage");
        assertThat(Files.readString(Path.of("src", "site", "assets", "site.css")))
                .contains("prefers-reduced-motion");

        assertThat(html).doesNotContain("href=\"./docs/user-guide.md\"");
        assertThat(html).doesNotContain("href=\"http://localhost:8080/swagger-ui.html\"");
        assertThat(html).doesNotContain("href=\"http://localhost:8080/actuator/health\"");
        assertThat(html).doesNotContain("Future Improvements");
    }
}
