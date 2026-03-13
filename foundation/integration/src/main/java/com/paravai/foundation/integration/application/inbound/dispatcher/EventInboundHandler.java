package com.paravai.foundation.integration.application.inbound.dispatcher;

import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import reactor.core.publisher.Mono;

public interface EventInboundHandler {

    boolean supports(DomainEventEnvelope<?> event);

    Mono<Void> handle(DomainEventEnvelope<?> event);
}