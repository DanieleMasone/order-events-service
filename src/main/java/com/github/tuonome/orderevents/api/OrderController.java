package com.github.tuonome.orderevents.api;

import com.github.tuonome.orderevents.api.dto.CreateOrderRequest;
import com.github.tuonome.orderevents.api.dto.OrderResponse;
import com.github.tuonome.orderevents.application.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * REST entry point for clients that create orders and trigger the event-driven flow.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order creation API")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Accepts a validated order creation command and returns the persisted resource location.
     *
     * @param request incoming order command
     * @return created order with Location header
     */
    @PostMapping
    @Operation(summary = "Create an order", description = "Persists an order and publishes an OrderCreatedEvent to Kafka.")
    @ApiResponse(responseCode = "201", description = "Order created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
    }
}
