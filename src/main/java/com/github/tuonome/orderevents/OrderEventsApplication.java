package com.github.tuonome.orderevents;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * Bootstraps the order events service and exposes shared infrastructure beans used by the application layer.
 */
@SpringBootApplication
public class OrderEventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderEventsApplication.class, args);
    }

    /**
     * Provides a UTC clock so time-dependent behavior can be deterministic in tests and consistent in persisted events.
     *
     * @return the application clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
