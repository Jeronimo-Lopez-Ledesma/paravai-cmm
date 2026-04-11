package com.paravai.communities.offer.infrastructure.event.handler;

import com.paravai.foundation.domain.event.DomainEventHandler;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.infrastructure.kafka.IntegrationEventPublisher;
import com.paravai.communities.offer.infrastructure.event.mapper.AuditTrailEnvelopeMapper;
import com.paravai.communities.offer.infrastructure.event.mapper.HistorizationEnvelopeMapper;
import com.paravai.communities.offer.infrastructure.event.mapper.OfferEventEnvelopeMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OfferEventPublisherHandler implements DomainEventHandler<EntityChangedEvent> {

    private static final Logger log = LoggerFactory.getLogger(OfferEventPublisherHandler.class);

    private final IntegrationEventPublisher publisher;

    private final AuditTrailEnvelopeMapper auditMapper;
    private final HistorizationEnvelopeMapper historizationMapper;
    private final OfferEventEnvelopeMapper integrationMapper;

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

        return Mono.when(audit, historization, integration).then();
    }

    private static String safeTraceId(EntityChangedEvent e) {
        return e.getTraceId() != null ? e.getTraceId().toString() : "no-trace";
    }

    private static String safeEntityId(EntityChangedEvent e) {
        return e.getEntityId() != null ? e.getEntityId().toString() : "no-entity";
    }
}