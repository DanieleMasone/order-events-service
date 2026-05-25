package com.github.tuonome.orderevents.infrastructure.kafka;

import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class KafkaConfigTest {

    @Test
    void listenerFactoryCommitsOffsetsAfterEachSuccessfulRecord() {
        KafkaConfig config = new KafkaConfig("order.created.v1.dlt", 1000, 3, false);
        CommonErrorHandler errorHandler = mock(CommonErrorHandler.class);

        var factory = config.kafkaListenerContainerFactory(consumerFactory(), errorHandler);

        assertThat(factory.getContainerProperties().getAckMode()).isEqualTo(ContainerProperties.AckMode.RECORD);
    }

    @Test
    void createsRetryAndDeadLetterErrorHandler() {
        KafkaConfig config = new KafkaConfig("order.created.v1.dlt", 1000, 3, true);

        CommonErrorHandler handler = config.orderKafkaErrorHandler(kafkaTemplate());

        assertThat(handler).isNotNull();
    }

    @Test
    void rejectsInvalidRetryConfiguration() {
        assertThatThrownBy(() -> new KafkaConfig("order.created.v1.dlt", 1000, 0, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max attempts");
    }

    @SuppressWarnings("unchecked")
    private static ConsumerFactory<String, OrderCreatedEvent> consumerFactory() {
        return mock(ConsumerFactory.class);
    }

    @SuppressWarnings("unchecked")
    private static KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }
}
