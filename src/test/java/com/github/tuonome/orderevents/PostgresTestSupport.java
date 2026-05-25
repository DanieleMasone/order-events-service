package com.github.tuonome.orderevents;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Shared PostgreSQL Testcontainer configuration for tests that need the production Flyway schema.
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class PostgresTestSupport {

    private static final String TESTCONTAINERS_ENV_STRATEGY =
            "org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy";

    static {
        if (System.getProperty("os.name").toLowerCase().contains("win")
                && System.getProperty("docker.host") == null
                && System.getenv("DOCKER_HOST") == null) {
            System.setProperty("docker.client.strategy", TESTCONTAINERS_ENV_STRATEGY);
            System.setProperty("docker.host", "npipe:////./pipe/dockerDesktopLinuxEngine");
        }
    }

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
