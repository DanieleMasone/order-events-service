package com.github.danielemasone.orderevents.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Kafka event emitted after an order has been accepted by the service and stored in PostgreSQL.
 */
public record OrderCreatedEvent(
        UUID eventId,
        String eventType,
        Instant occurredAt,
        UUID orderId,
        String customerId,
        BigDecimal amount
) {
}
