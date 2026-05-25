package com.github.tuonome.orderevents.infrastructure.kafka;

import com.github.tuonome.orderevents.domain.OrderCreatedEvent;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka listener configuration for manual failure handling, retry backoff, and dead-letter publishing.
 */
@Configuration
public class KafkaConfig {

    private final String dltTopic;
    private final long retryBackoffMs;
    private final long retryMaxAttempts;
    private final boolean listenerAutoStartup;

    public KafkaConfig(
            @Value("${app.kafka.topics.order-created-dlt}") String dltTopic,
            @Value("${app.kafka.retry.backoff-ms}") long retryBackoffMs,
            @Value("${app.kafka.retry.max-attempts}") long retryMaxAttempts,
            @Value("${spring.kafka.listener.auto-startup:true}") boolean listenerAutoStartup
    ) {
        if (retryMaxAttempts < 1) {
            throw new IllegalArgumentException("Kafka retry max attempts must be at least 1");
        }
        this.dltTopic = dltTopic;
        this.retryBackoffMs = retryBackoffMs;
        this.retryMaxAttempts = retryMaxAttempts;
        this.listenerAutoStartup = listenerAutoStartup;
    }

    /**
     * Configures the listener factory so offsets are committed per record only after successful listener execution.
     *
     * @param consumerFactory Spring Kafka consumer factory
     * @param orderKafkaErrorHandler retry and DLT handler
     * @return listener container factory used by the order-created consumer
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, OrderCreatedEvent> consumerFactory,
            CommonErrorHandler orderKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(orderKafkaErrorHandler);
        factory.setAutoStartup(listenerAutoStartup);
        return factory;
    }

    /**
     * Retries transient listener failures with fixed backoff before publishing exhausted records to the DLT topic.
     *
     * @param kafkaTemplate template used by the recoverer to publish failed records
     * @return Spring Kafka error handler
     */
    @Bean
    public CommonErrorHandler orderKafkaErrorHandler(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(dltTopic, record.partition())
        );
        return new DefaultErrorHandler(recoverer, new FixedBackOff(retryBackoffMs, retryMaxAttempts - 1));
    }
}
