package com.paravai.foundation.infrastructure.event;

import com.paravai.foundation.domain.event.DomainEvent;
import com.paravai.foundation.domain.event.ReactiveDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DispatcherDomainEventPublisher implements ReactiveDomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DispatcherDomainEventPublisher.class);
    private final DomainEventDispatcher dispatcher;

    @Override
    public <T extends DomainEvent> Mono<Void> publish(T event) {
        if (event == null) {
            return Mono.error(new IllegalArgumentException("event cannot be null"));
        }

        log.info("Dispatching DomainEvent eventId={} type={} occurredAt={}",
                event.metadata().eventId(),
                event.getClass().getSimpleName(),
                event.metadata().occurredOn().getInstant());

        return dispatcher.dispatch(event)
                .doOnSuccess(v -> log.info("DomainEvent dispatched successfully eventId={} type={}",
                        event.metadata().eventId(),
                        event.getClass().getSimpleName()))
                .doOnError(ex -> log.error("DomainEvent dispatch failed eventId={} type={}",
                        event.metadata().eventId(),
                        event.getClass().getSimpleName(),
                        ex));
    }

    @Override
    public Mono<Void> publishAll(List<? extends DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return Mono.empty();
        }

        log.info("Dispatching batch of DomainEvents size={}", events.size());

        return Flux.fromIterable(events)
                .concatMap(event ->
                        dispatcher.dispatch(event)
                                .doOnSuccess(v -> log.info("DomainEvent dispatched in batch eventId={} type={}",
                                        event.metadata().eventId(),
                                        event.getClass().getSimpleName()))
                                .doOnError(ex -> log.error("DomainEvent dispatch failed in batch eventId={} type={}",
                                        event.metadata().eventId(),
                                        event.getClass().getSimpleName(),
                                        ex))
                )
                .then()
                .doOnSuccess(v -> log.debug("Batch DomainEvent dispatch completed size={}", events.size()));
    }
}
