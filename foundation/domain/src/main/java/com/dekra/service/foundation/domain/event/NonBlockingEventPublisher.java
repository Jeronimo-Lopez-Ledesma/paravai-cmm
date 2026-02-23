package com.paravai.foundation.domaincore.event;

import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.domaincore.value.OidValue;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Non-blocking wrapper around ReactiveDomainEventPublisher.
 * This component ensures that publishing a EntityChangedEvent
 * does not affect the command execution path.
 *
 * In a distributed architecture, emitting domain events (for audit,
 * historization, integration, etc.) is considered a side effect of a
 * successful state change. However, failures in the event infrastructure
 * (Kafka, handlers, downstream services) must not compromise the primary
 * business transaction.
 *
 * This publisher applies a best-effort strategy: (https://martinfowler.com/articles/201701-event-driven.html):
 *     Attempts to publish the event using ReactiveDomainEventPublisher
 *     Logs success or failure with contextual information
 *     Swallows errors using onErrorResume to prevent propagation
 *
 * It is intended to be reused across all CMMs and application services
 * to enforce consistent non-blocking event publication semantics.
 *
 * IMPORTANT:
 * This component should only be used at the application boundary
 * (command services). Domain logic must remain unaware of publishing concerns.
 */

public final class NonBlockingEventPublisher {

    private final ReactiveDomainEventPublisher publisher;
    private final Logger log;

    public NonBlockingEventPublisher(ReactiveDomainEventPublisher publisher, Logger log) {
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.log = Objects.requireNonNull(log, "log");
    }

    public Mono<Void> publish(EntityChangedEvent evt) {
        Objects.requireNonNull(evt, "evt");

        IdValue traceId = evt.getTraceId();
        OidValue userOid = evt.getUserOid();
        Object entityId = evt.getEntityId();

        return publisher.publish(evt)
                .doOnSuccess(v -> log.debug("[{}][{}] Published EntityChangedEvent op={} entityId={}",
                        traceId, userOid, evt.getOperationType(), entityId))
                .onErrorResume(e -> {
                    log.warn("[{}][{}] Failed to publish EntityChangedEvent op={} entityId={} (non-blocking)",
                            traceId, userOid, evt.getOperationType(), entityId, e);
                    return Mono.empty();
                });
    }
}