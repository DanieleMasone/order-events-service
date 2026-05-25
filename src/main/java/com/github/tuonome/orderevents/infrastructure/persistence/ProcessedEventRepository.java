package com.github.tuonome.orderevents.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository backing the consumer idempotency store keyed by Kafka event identifier.
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {
}
