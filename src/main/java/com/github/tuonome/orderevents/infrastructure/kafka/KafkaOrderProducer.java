package com.github.tuonome.orderevents.infrastructure.kafka;

import com.github.tuonome.orderevents.application.EventPublisher;
import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka implementation of the event publisher port for order-created events.
 */
@Component
public class KafkaOrderProducer implements EventPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String topic;

    public KafkaOrderProducer(
            KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.order-created}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Sends the event synchronously enough to surface broker send failures to the order creation transaction.
     * The event identifier is used as the Kafka key so duplicate deliveries retain a stable partitioning key.
     *
     * @param event event to publish
     */
    @Override
    public void publish(OrderCreatedEvent event) {
        try {
            kafkaTemplate.send(topic, event.eventId().toString(), event).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing order-created event", ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new IllegalStateException("Failed to publish order-created event", ex);
        }
    }
}
