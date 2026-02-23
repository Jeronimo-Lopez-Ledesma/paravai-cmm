package com.dekra.service.foundation.infrastructure.kafka;


import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import reactor.core.publisher.Mono;

import java.util.List;
/**
 * Abstraction for publishing integration events to the platform's messaging
 * infrastructure (e.g., Kafka). Implementations of this interface are
 * responsible for delivering {@link DomainEventEnvelope} instances to the
 * configured event bus in a reactive and non-blocking manner.
 * <p>
 * This publisher is used by bounded contexts to expose domain changes as
 * integration events that other services can consume asynchronously,
 * enabling loose coupling and event-driven interactions across the system.
 */
public interface IntegrationEventPublisher {

    /**
     * Publishes a single {@link DomainEventEnvelope} to the messaging system.
     * <p>
     * Implementations must handle serialization, partition routing, and
     * delivery guarantees according to the platform's event contract.
     *
     * @param envelope the event to publish
     * @param <T>      the payload type inside the envelope
     * @return a {@link Mono} that completes when the event has been dispatched
     */
    <T> Mono<Void> publish(DomainEventEnvelope<T> envelope);

    /**
     * Publishes multiple {@link DomainEventEnvelope} instances in a batch.
     * <p>
     * This method allows optimized delivery for scenarios where several
     * domain events are emitted together, minimizing I/O overhead.
     *
     * @param envelopes the list of events to publish
     * @return a {@link Mono} that completes when all events have been dispatched
     */
    Mono<Void> publishAll(List<? extends DomainEventEnvelope<?>> envelopes);
}
