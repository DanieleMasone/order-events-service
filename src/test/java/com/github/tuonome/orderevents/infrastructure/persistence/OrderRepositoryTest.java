package com.github.tuonome.orderevents.infrastructure.persistence;

import com.github.tuonome.orderevents.PostgresTestSupport;
import com.github.tuonome.orderevents.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest extends PostgresTestSupport {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Test
    void persistsOrderRowsInPostgres() {
        OrderEntity entity = new OrderEntity();
        entity.setId(UUID.randomUUID());
        entity.setCustomerId("customer-001");
        entity.setAmount(new BigDecimal("199.90"));
        entity.setStatus(OrderStatus.CREATED);
        entity.setCreatedAt(Instant.parse("2026-05-20T10:15:30Z"));

        orderRepository.saveAndFlush(entity);

        assertThat(orderRepository.findById(entity.getId()))
                .isPresent()
                .get()
                .extracting(OrderEntity::getCustomerId)
                .isEqualTo("customer-001");
    }

    @Test
    void enforcesUniqueProcessedEventIds() {
        UUID eventId = UUID.randomUUID();
        processedEventRepository.saveAndFlush(new ProcessedEventEntity(eventId, "OrderCreated", Instant.now()));

        assertThatThrownBy(() -> processedEventRepository.saveAndFlush(
                new ProcessedEventEntity(eventId, "OrderCreated", Instant.now())
        )).isInstanceOf(DataIntegrityViolationException.class);
    }
}
