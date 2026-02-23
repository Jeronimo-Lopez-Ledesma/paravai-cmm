package com.dekra.service.foundation.integration.application;

import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import reactor.core.publisher.Mono;

/**
 * Generic contract for projecting domain events into read models.
 * Implementations decide how to hydrate and persist their read models.
 */
public interface IntegrationProjectionService {

    Mono<Void> handleCreate(DomainEventEnvelope<?> envelope);

    Mono<Void> handleUpdate(DomainEventEnvelope<?> envelope);

    Mono<Void> handleDelete(DomainEventEnvelope<?> envelope);

    /**
     * Default dispatcher based on changeType.
     * Implementations only need to implement create/update/delete.
     */
    default Mono<Void> handleEvent(DomainEventEnvelope<?> envelope) {
        String changeType = envelope.getChangeType();
        if (changeType == null) {
            return Mono.empty();
        }

        return switch (changeType.trim().toUpperCase()) {
            case "CREATED", "CREATE" -> handleCreate(envelope);
            case "UPDATED", "UPDATE" -> handleUpdate(envelope);
            case "DELETED", "DELETE" -> handleDelete(envelope);
            default -> Mono.empty();
        };
    }
}
