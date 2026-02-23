package com.paravai.foundation.domain.event;

import reactor.core.publisher.Mono;

public interface ReactiveDomainEventHandler<T extends DomainEvent> {

    Mono<Void> handle(T event);

    Class<T> eventType();
}
