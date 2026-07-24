package com.github.danielemasone.orderevents.application;

import com.github.danielemasone.orderevents.api.dto.CreateOrderRequest;
import com.github.danielemasone.orderevents.api.dto.OrderResponse;
import com.github.danielemasone.orderevents.domain.OrderCreatedEvent;
import com.github.danielemasone.orderevents.domain.OrderStatus;
import com.github.danielemasone.orderevents.infrastructure.mapping.OrderMapper;
import com.github.danielemasone.orderevents.infrastructure.persistence.OrderEntity;
import com.github.danielemasone.orderevents.infrastructure.persistence.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-05-20T10:15:30Z");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisher eventPublisher;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        OrderMapper mapper = Mappers.getMapper(OrderMapper.class);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        orderService = new OrderService(orderRepository, mapper, eventPublisher, clock);
    }

    @Test
    void createsOrderAndPublishesOrderCreatedEvent() {
        when(orderRepository.saveAndFlush(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(new CreateOrderRequest("customer-001", new BigDecimal("199.90")));

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(orderRepository).saveAndFlush(orderCaptor.capture());
        verify(eventPublisher).publish(eventCaptor.capture());

        OrderEntity persisted = orderCaptor.getValue();
        OrderCreatedEvent event = eventCaptor.getValue();
        assertThat(response.id()).isEqualTo(persisted.getId());
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(event.eventId()).isNotNull();
        assertThat(event.eventType()).isEqualTo("OrderCreated");
        assertThat(event.orderId()).isEqualTo(response.id());
        assertThat(event.customerId()).isEqualTo("customer-001");
        assertThat(event.amount()).isEqualByComparingTo("199.90");
        assertThat(event.occurredAt()).isEqualTo(NOW);
    }
}
