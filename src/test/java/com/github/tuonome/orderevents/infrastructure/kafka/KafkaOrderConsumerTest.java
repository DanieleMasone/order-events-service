package com.github.tuonome.orderevents.infrastructure.kafka;

import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import com.github.tuonome.orderevents.infrastructure.persistence.ProcessedEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaOrderConsumerTest {

    private static final Instant NOW = Instant.parse("2026-05-20T10:15:30Z");

    @Mock
    private ProcessedEventRepository processedEventRepository;

    private KafkaOrderConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new KafkaOrderConsumer(processedEventRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void recordsNewEventAsProcessed() {
        OrderCreatedEvent event = event();
        when(processedEventRepository.insertIfAbsent(event.eventId(), event.eventType(), NOW)).thenReturn(1);

        consumer.consume(event);

        verify(processedEventRepository).insertIfAbsent(event.eventId(), event.eventType(), NOW);
    }

    @Test
    void skipsDuplicateEventWithoutSideEffects() {
        OrderCreatedEvent event = event();
        when(processedEventRepository.insertIfAbsent(event.eventId(), event.eventType(), NOW)).thenReturn(0);

        consumer.consume(event);

        verify(processedEventRepository).insertIfAbsent(event.eventId(), event.eventType(), NOW);
    }

    @Test
    void propagatesTransientPersistenceFailureForKafkaRetry() {
        OrderCreatedEvent event = event();
        when(processedEventRepository.insertIfAbsent(event.eventId(), event.eventType(), NOW))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThatThrownBy(() -> consumer.consume(event))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    private static OrderCreatedEvent event() {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                "OrderCreated",
                Instant.parse("2026-05-20T10:15:29Z"),
                UUID.randomUUID(),
                "customer-001",
                new BigDecimal("199.90")
        );
    }
}
