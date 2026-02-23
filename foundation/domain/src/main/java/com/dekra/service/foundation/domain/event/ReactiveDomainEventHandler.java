package com.paravai.foundation.domaincore.event;

import reactor.core.publisher.Mono;

public interface ReactiveDomainEventHandler<T extends DomainEvent> {

    Mono<Void> handle(T event);

    Class<T> eventType();
}
