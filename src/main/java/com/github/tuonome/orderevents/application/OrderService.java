package com.github.tuonome.orderevents.application;

import com.github.tuonome.orderevents.api.dto.CreateOrderRequest;
import com.github.tuonome.orderevents.api.dto.OrderResponse;
import com.github.tuonome.orderevents.domain.Order;
import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import com.github.tuonome.orderevents.domain.OrderStatus;
import com.github.tuonome.orderevents.infrastructure.mapping.OrderMapper;
import com.github.tuonome.orderevents.infrastructure.persistence.OrderEntity;
import com.github.tuonome.orderevents.infrastructure.persistence.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

/**
 * Coordinates order creation by persisting the aggregate and publishing the corresponding Kafka event.
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper, EventPublisher eventPublisher, Clock clock) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /**
     * Creates an order inside a database transaction, flushes it to PostgreSQL, and then publishes the event.
     * This deliberately keeps the project simple instead of introducing an outbox; the trade-off is that the
     * database write and Kafka write are coordinated by application flow rather than a single atomic resource.
     *
     * @param request validated API request
     * @return persisted order representation
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(
                UUID.randomUUID(),
                request.customerId(),
                request.amount(),
                OrderStatus.CREATED,
                clock.instant()
        );

        OrderEntity persisted = orderRepository.saveAndFlush(orderMapper.toEntity(order));
        Order createdOrder = orderMapper.toDomain(persisted);
        OrderCreatedEvent event = orderMapper.toCreatedEvent(createdOrder, UUID.randomUUID(), clock.instant());
        eventPublisher.publish(event);
        return orderMapper.toResponse(createdOrder);
    }
}
