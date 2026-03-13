package com.paravai.foundation.integration.application.inbound.dispatcher;

import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class EventInboundDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventInboundDispatcher.class);

    private final List<EventInboundHandler> handlers;

    public EventInboundDispatcher(List<EventInboundHandler> handlers) {
        this.handlers = Objects.requireNonNull(handlers, "handlers is required");
        log.info("EventInboundDispatcher initialized with {} handlers: {}",
                handlers.size(),
                handlers.stream().map(h -> h.getClass().getName()).toList());
    }

    public Mono<Void> dispatch(DomainEventEnvelope<?> event) {
        if (event == null) {
            return Mono.empty();
        }

        List<EventInboundHandler> matchingHandlers = handlers.stream()
                .filter(handler -> handler.supports(event))
                .toList();

        if (matchingHandlers.isEmpty()) {
            return Mono.empty();
        }

        if (matchingHandlers.size() > 1) {
            return Mono.error(new EventInboundDispatchException(
                    "Multiple inbound handlers found for event: schemaId=%s, entityType=%s, changeType=%s, handlers=%s"
                            .formatted(
                                    event.getSchemaId(),
                                    event.getEntityType(),
                                    event.getChangeType(),
                                    matchingHandlers.stream()
                                            .map(h -> h.getClass().getName())
                                            .toList()
                            )
            ));
        }

        return matchingHandlers.getFirst().handle(event);
    }
}