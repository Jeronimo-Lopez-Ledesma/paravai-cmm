package com.dekra.service.foundation.integration.application;

import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IntegrationEventHandler {
    Mono<Void> handle(DomainEventEnvelope<?> envelope);
}