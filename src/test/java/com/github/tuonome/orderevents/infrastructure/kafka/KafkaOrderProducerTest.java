package com.github.tuonome.orderevents.infrastructure.kafka;

import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaOrderProducerTest {

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Test
    void publishesEventUsingEventIdAsKafkaKey() {
        OrderCreatedEvent event = event();
        when(kafkaTemplate.send("order.created.v1", event.eventId().toString(), event))
                .thenReturn(CompletableFuture.completedFuture(sendResult()));

        new KafkaOrderProducer(kafkaTemplate, "order.created.v1").publish(event);

        verify(kafkaTemplate).send("order.created.v1", event.eventId().toString(), event);
    }

    @Test
    void wrapsBrokerPublicationFailure() {
        OrderCreatedEvent event = event();
        CompletableFuture<SendResult<String, OrderCreatedEvent>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send("order.created.v1", event.eventId().toString(), event)).thenReturn(failed);

        KafkaOrderProducer producer = new KafkaOrderProducer(kafkaTemplate, "order.created.v1");

        assertThatThrownBy(() -> producer.publish(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to publish");
    }

    private static OrderCreatedEvent event() {
        return new OrderCreatedEvent(
                UUID.randomUUID(),
                "OrderCreated",
                Instant.parse("2026-05-20T10:15:30Z"),
                UUID.randomUUID(),
                "customer-001",
                new BigDecimal("199.90")
        );
    }

    @SuppressWarnings("unchecked")
    private static SendResult<String, OrderCreatedEvent> sendResult() {
        return mock(SendResult.class);
    }
}
