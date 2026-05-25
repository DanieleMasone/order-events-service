package com.github.tuonome.orderevents.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request payload accepted by the REST API when a client asks the service to create a new order.
 */
@Schema(description = "Payload used to create an order and publish its creation event.")
public record CreateOrderRequest(
        @Schema(description = "External customer identifier.", example = "customer-001",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String customerId,

        @Schema(description = "Order amount. Must be greater than zero.", example = "199.90",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {
}
