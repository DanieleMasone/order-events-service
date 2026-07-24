package com.github.danielemasone.orderevents.infrastructure.mapping;

import com.github.danielemasone.orderevents.api.dto.OrderResponse;
import com.github.danielemasone.orderevents.domain.Order;
import com.github.danielemasone.orderevents.domain.OrderCreatedEvent;
import com.github.danielemasone.orderevents.domain.OrderStatus;
import com.github.danielemasone.orderevents.infrastructure.persistence.OrderEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);

    @Test
    void mapsOrderAcrossPersistenceApiAndEventContracts() {
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-20T10:15:30Z");
        Instant occurredAt = Instant.parse("2026-05-20T10:15:31Z");
        Order order = new Order(orderId, "customer-001", new BigDecimal("199.90"), OrderStatus.CREATED, createdAt);

        OrderEntity entity = mapper.toEntity(order);
        Order domain = mapper.toDomain(entity);
        OrderResponse response = mapper.toResponse(domain);
        OrderCreatedEvent event = mapper.toCreatedEvent(domain, eventId, occurredAt);

        assertThat(entity.getId()).isEqualTo(orderId);
        assertThat(domain).isEqualTo(order);
        assertThat(response.id()).isEqualTo(orderId);
        assertThat(response.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(event.eventId()).isEqualTo(eventId);
        assertThat(event.eventType()).isEqualTo("OrderCreated");
        assertThat(event.orderId()).isEqualTo(orderId);
        assertThat(event.occurredAt()).isEqualTo(occurredAt);
    }
}
