package com.github.danielemasone.orderevents.api.dto;

import com.github.danielemasone.orderevents.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Stable API representation returned after an order has been persisted and its creation event has been requested.
 */
@Schema(description = "Created order representation returned by the API.")
public record OrderResponse(
        @Schema(description = "Service-generated order identifier.")
        UUID id,

        @Schema(description = "External customer identifier.")
        String customerId,

        @Schema(description = "Order amount.")
        BigDecimal amount,

        @Schema(description = "Current order status.")
        OrderStatus status,

        @Schema(description = "UTC timestamp captured when the order was created.")
        Instant createdAt
) {
}
