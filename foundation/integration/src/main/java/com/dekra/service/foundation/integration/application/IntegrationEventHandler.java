package com.paravai.foundation.integration.application;

import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IntegrationEventHandler {
    Mono<Void> handle(DomainEventEnvelope<?> envelope);
}