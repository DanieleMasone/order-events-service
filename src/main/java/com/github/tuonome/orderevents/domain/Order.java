package com.github.tuonome.orderevents.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for an order owned by the service before it is translated to persistence, API, or event contracts.
 */
public record Order(
        UUID id,
        String customerId,
        BigDecimal amount,
        OrderStatus status,
        Instant createdAt
) {
}
