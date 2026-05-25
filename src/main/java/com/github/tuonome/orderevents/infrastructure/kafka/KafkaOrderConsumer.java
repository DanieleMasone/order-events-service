package com.github.tuonome.orderevents.infrastructure.kafka;

import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import com.github.tuonome.orderevents.infrastructure.persistence.ProcessedEventEntity;
import com.github.tuonome.orderevents.infrastructure.persistence.ProcessedEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

/**
 * Kafka consumer that records processed event identifiers to provide idempotent at-least-once processing.
 */
@Component
public class KafkaOrderConsumer {

    private final ProcessedEventRepository processedEventRepository;
    private final Clock clock;

    public KafkaOrderConsumer(ProcessedEventRepository processedEventRepository, Clock clock) {
        this.processedEventRepository = processedEventRepository;
        this.clock = clock;
    }

    /**
     * Processes the event inside a database transaction and returns normally only after the idempotency marker is stored.
     * Spring Kafka commits the offset after the listener succeeds; exceptions are left unhandled so the configured
     * retry and dead-letter behavior can take over.
     *
     * @param event consumed Kafka payload
     */
    @Transactional
    @KafkaListener(
            topics = "${app.kafka.topics.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(OrderCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            return;
        }

        try {
            processedEventRepository.saveAndFlush(
                    new ProcessedEventEntity(event.eventId(), event.eventType(), clock.instant())
            );
        } catch (DataIntegrityViolationException duplicateEvent) {
            // A concurrent consumer may have inserted the marker after the first lookup; only that race is idempotent.
            if (processedEventRepository.existsById(event.eventId())) {
                return;
            }
            throw duplicateEvent;
        }
    }
}
