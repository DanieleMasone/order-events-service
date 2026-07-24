package com.github.tuonome.orderevents.infrastructure.kafka;

import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaConfigTest {

    @Test
    void listenerFactoryCommitsOffsetsAfterEachSuccessfulRecord() {
        KafkaConfig config = new KafkaConfig("order.created.v1.dlt", 1000, 3, false);
        CommonErrorHandler errorHandler = mock(CommonErrorHandler.class);

        var factory = config.kafkaListenerContainerFactory(consumerFactory(), errorHandler);

        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.RECORD);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void publishesToDltOnlyAfterConfiguredAttemptsAreExhausted() {
        String dltTopic = "order.created.v1.dlt";
        KafkaTemplate<String, OrderCreatedEvent> template = kafkaTemplate();
        when(template.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        Consumer<String, OrderCreatedEvent> consumer = mock(Consumer.class);
        when(consumer.partitionsFor(eq(dltTopic), any(Duration.class)))
                .thenReturn(List.of(new PartitionInfo(dltTopic, 2, null, null, null)));

        DefaultErrorHandler handler = (DefaultErrorHandler) new KafkaConfig(dltTopic, 0, 3, true)
                .orderKafkaErrorHandler(template);
        OrderCreatedEvent event = event();
        ConsumerRecord<String, OrderCreatedEvent> record =
                new ConsumerRecord<>("order.created.v1", 2, 10L, event.eventId().toString(), event);
        MessageListenerContainer container = mock(MessageListenerContainer.class);
        RuntimeException failure = new RuntimeException("processing failed");

        assertThat(handler.handleOne(failure, record, consumer, container)).isFalse();
        assertThat(handler.handleOne(failure, record, consumer, container)).isFalse();
        verify(template, never()).send(any(ProducerRecord.class));

        assertThat(handler.handleOne(failure, record, consumer, container)).isTrue();

        ArgumentCaptor<ProducerRecord<String, OrderCreatedEvent>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(template).send(captor.capture());
        assertThat(captor.getValue().topic()).isEqualTo(dltTopic);
        assertThat(captor.getValue().partition()).isEqualTo(2);
        assertThat(captor.getValue().key()).isEqualTo(event.eventId().toString());
        assertThat(captor.getValue().value()).isEqualTo(event);
    }

    @Test
    void rejectsInvalidRetryConfiguration() {
        assertThatThrownBy(() -> new KafkaConfig("order.created.v1.dlt", 1000, 0, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max attempts");

        assertThatThrownBy(() -> new KafkaConfig("order.created.v1.dlt", -1, 3, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backoff");
    }

    @SuppressWarnings("unchecked")
    private static ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {
        return mock(ConsumerFactory.class);
    }

    @SuppressWarnings("unchecked")
    private static KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {
        return mock(KafkaTemplate.class);
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
}
