package com.paravai.foundation.domaincore.event;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive publisher for domain events.
 *
 * This interface defines how domain events are emitted inside the domain layer
 * using a non-blocking reactive API. Implementations decide how events are
 * dispatched (in-memory, messaging, Kafka, etc.).
 *
 * The default publishAll method sends all events in parallel and completes when
 * all of them have been published.
 */
public interface ReactiveDomainEventPublisher {

    /**
     * Publishes a single domain event reactively.
     *
     * @param event the event to publish
     * @param <T>   the event type
     * @return a Mono that completes when the event has been dispatched
     */
    <T extends DomainEvent> Mono<Void> publish(T event);

    /**
     * Publishes multiple domain events reactively.
     * Uses Mono.when to trigger all publish operations in parallel.
     *
     * @param events list of events to publish
     * @return a Mono that completes when all events have been dispatched
     */
    default Mono<Void> publishAll(List<? extends DomainEvent> events) {
        return Mono.defer(() ->
                Mono.when(events.stream().map(this::publish).toList())
        );
    }
}
