package com.dekra.service.foundation.domaincore.event;

import reactor.core.publisher.Mono;

// Generic interface for handling domain events of a specific type
public interface DomainEventHandler<T extends DomainEvent> {


    Mono<Void> handle(T event);
}
