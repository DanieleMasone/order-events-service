package com.github.tuonome.orderevents.infrastructure.mapping;

import com.github.tuonome.orderevents.api.dto.OrderResponse;
import com.github.tuonome.orderevents.domain.Order;
import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import com.github.tuonome.orderevents.infrastructure.persistence.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.UUID;

/**
 * MapStruct boundary for translating between API, domain, persistence, and Kafka event representations.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /**
     * Converts the domain model to the JPA persistence model without applying business decisions.
     *
     * @param order domain order
     * @return persistence entity
     */
    OrderEntity toEntity(Order order);

    /**
     * Rehydrates a domain order from the persistence model.
     *
     * @param entity persisted entity
     * @return domain order
     */
    Order toDomain(OrderEntity entity);

    /**
     * Converts the domain order to the stable REST response contract.
     *
     * @param order domain order
     * @return API response
     */
    OrderResponse toResponse(Order order);

    /**
     * Projects a persisted order into the Kafka event envelope used by downstream consumers.
     *
     * @param order persisted order
     * @param eventId unique event identifier used by consumers for idempotency
     * @param occurredAt UTC event timestamp
     * @return order-created event
     */
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "eventType", constant = "OrderCreated")
    @Mapping(target = "occurredAt", source = "occurredAt")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "customerId", source = "order.customerId")
    @Mapping(target = "amount", source = "order.amount")
    OrderCreatedEvent toCreatedEvent(Order order, UUID eventId, Instant occurredAt);
}
