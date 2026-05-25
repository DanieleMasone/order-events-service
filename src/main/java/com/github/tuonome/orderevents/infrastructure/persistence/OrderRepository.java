package com.github.tuonome.orderevents.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * PostgreSQL repository for the order aggregate persistence model.
 */
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}
