package com.github.tuonome.orderevents.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository backing the consumer idempotency store keyed by Kafka event identifier.
 */
public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, UUID> {

    /**
     * Atomically claims an event ID using PostgreSQL's unique primary key. Returning zero identifies a duplicate
     * without aborting the surrounding transaction, which lets the Kafka listener complete as an idempotent no-op.
     *
     * @param eventId event identifier used as the idempotency key
     * @param eventType event contract name recorded for diagnostics
     * @param processedAt time at which processing was claimed
     * @return one when the marker was inserted, or zero when the event was already processed
     */
    @Modifying
    @Query(value = """
            INSERT INTO processed_events (event_id, event_type, processed_at)
            VALUES (:eventId, :eventType, :processedAt)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType,
            @Param("processedAt") Instant processedAt
    );
}
