package com.github.danielemasone.orderevents;

import com.github.danielemasone.orderevents.domain.OrderCreatedEvent;
import com.github.danielemasone.orderevents.infrastructure.persistence.OrderRepository;
import com.github.danielemasone.orderevents.infrastructure.persistence.ProcessedEventRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
                + "org.springframework.boot.data.jpa.autoconfigure.JpaRepositoriesAutoConfiguration"
})
class OrderEventsApplicationTests {

    @Autowired
    private KafkaProperties kafkaProperties;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private ProcessedEventRepository processedEventRepository;

    @Test
    void configuredKafkaJsonPreservesEventContractWithoutTypeHeaders() {
        var producerProperties = kafkaProperties.buildProducerProperties();
        var consumerProperties = kafkaProperties.buildConsumerProperties();
        assertThat(producerProperties.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG))
                .isEqualTo(JacksonJsonSerializer.class);
        assertThat(consumerProperties.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG))
                .isEqualTo(JacksonJsonDeserializer.class);

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.fromString("3c1395e6-38b1-4142-8638-026e713184d4"), "OrderCreatedEvent",
                Instant.parse("2026-01-01T12:00:00Z"),
                UUID.fromString("b4036e81-3eb1-4ea0-a13e-af1291e85580"), "customer-001",
                new BigDecimal("199.90")
        );
        RecordHeaders headers = new RecordHeaders();
        try (var serializer = new JacksonJsonSerializer<OrderCreatedEvent>();
             var deserializer = new JacksonJsonDeserializer<OrderCreatedEvent>()) {
            serializer.configure(producerProperties, false);
            deserializer.configure(consumerProperties, false);
            byte[] json = serializer.serialize("order.created.v1", headers, event);

            assertThat(headers).isEmpty();
            assertThat(deserializer.deserialize("order.created.v1", headers, json)).isEqualTo(event);
        }
    }
}
