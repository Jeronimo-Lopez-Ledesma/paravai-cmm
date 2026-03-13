package com.paravai.foundation.integration.application.inbound;

import com.paravai.foundation.integration.application.inbound.dispatcher.EventInboundDispatcher;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class InboundEventConsumer {

    private final EventInboundDispatcher dispatcher;

    public InboundEventConsumer(EventInboundDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Mono<Void> consume(DomainEventEnvelope<?> event) {
        return dispatcher.dispatch(event);
    }
}