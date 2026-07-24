package com.github.danielemasone.orderevents;

import com.github.danielemasone.orderevents.infrastructure.persistence.OrderRepository;
import com.github.danielemasone.orderevents.infrastructure.persistence.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class OrderEventsApplicationTests {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private ProcessedEventRepository processedEventRepository;

    @Test
    void contextLoads() {
    }
}
