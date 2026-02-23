package com.dekra.service.regulations.standards.relationships.infrastructure.event.handler;

import com.dekra.service.foundation.domaincore.event.DomainEventHandler;
import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.infrastructure.kafka.IntegrationEventPublisher;
import com.dekra.service.regulations.standards.relationships.infrastructure.event.mapper.AuditTrailEnvelopeMapper;
import com.dekra.service.regulations.standards.relationships.infrastructure.event.mapper.HistorizationEnvelopeMapper;
import com.dekra.service.regulations.standards.relationships.infrastructure.event.mapper.StandardRelationshipEventEnvelopeMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Single platform handler for Standards:
 * publishes all platform channels derived from EntityChangedEvent:
 *  - AUDIT
 *  - HISTORIZATION
 *  - INTEGRATION
 *
 * Best-effort by design: failures in any channel must not fail the command flow.
 */
@Component
@RequiredArgsConstructor
public class StandardRelationshipEventPublisherHandler implements DomainEventHandler<EntityChangedEvent> {

    private static final Logger log = LoggerFactory.getLogger(StandardRelationshipEventPublisherHandler.class);

    private final IntegrationEventPublisher publisher;

    private final AuditTrailEnvelopeMapper auditMapper;
    private final HistorizationEnvelopeMapper historizationMapper;
    private final StandardRelationshipEventEnvelopeMapper integrationMapper;

    @Override
    public Mono<Void> handle(EntityChangedEvent event) {

        Mono<Void> audit = publisher.publish(auditMapper.map(event))
                .doOnError(ex -> log.warn("[{}][{}] Failed to publish AUDIT envelope",
                        safeTraceId(event), safeEntityId(event), ex))
                .onErrorResume(ex -> Mono.empty());

        Mono<Void> historization = publisher.publish(historizationMapper.map(event))
                .doOnError(ex -> log.warn("[{}][{}] Failed to publish HISTORIZATION envelope",
                        safeTraceId(event), safeEntityId(event), ex))
                .onErrorResume(ex -> Mono.empty());

        Mono<Void> integration = publisher.publish(integrationMapper.map(event))
                .doOnError(ex -> log.warn("[{}][{}] Failed to publish INTEGRATION envelope",
                        safeTraceId(event), safeEntityId(event), ex))
                .onErrorResume(ex -> Mono.empty());

        // Best-effort: run them all, complete regardless of individual failures.
        return Mono.when(audit, historization, integration).then();
    }

    private static String safeTraceId(EntityChangedEvent e) {
        return e.getTraceId() != null ? e.getTraceId().toString() : "no-trace";
    }

    private static String safeEntityId(EntityChangedEvent e) {
        return e.getEntityId() != null ? e.getEntityId().toString() : "no-entity";
    }
}
