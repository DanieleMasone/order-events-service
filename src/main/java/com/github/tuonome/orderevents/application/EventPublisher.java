package com.github.tuonome.orderevents.application;

import com.github.tuonome.orderevents.domain.OrderCreatedEvent;

/**
 * Application port used by the order workflow to publish domain events without depending on a specific broker client.
 */
public interface EventPublisher {

    /**
     * Publishes an order-created event as an externally visible side effect of the order creation transaction.
     *
     * @param event event to publish
     */
    void publish(OrderCreatedEvent event);
}
