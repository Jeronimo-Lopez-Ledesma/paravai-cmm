package com.paravai.foundation.integration.application;

import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import reactor.core.publisher.Mono;
/**
 * Defines the contract for routing integration events inside a read-side application.
 *
 * An IntegrationEventRouter receives a DomainEventEnvelope coming from the message
 * infrastructure (typically Kafka) and determines which projection service or handler
 * must process it.
 *
 * The router abstracts the dispatching logic so that consumers do not need to know
 * which component is responsible for handling each event type or entity type.
 */
public interface IntegrationEventRouter {
    /**
     * Routes the given integration event to its corresponding handler.
     *
     * Implementations typically inspect the entity type, change type, or payload
     * contained in the DomainEventEnvelope to determine the correct projection service.
     *
     * @param envelope the deserialized integration event
     * @return a Mono that completes once the event has been routed and processed
     */
    Mono<Void> route(DomainEventEnvelope<?> envelope);
}
