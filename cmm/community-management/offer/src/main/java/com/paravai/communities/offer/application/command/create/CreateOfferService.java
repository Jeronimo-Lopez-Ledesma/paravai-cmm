package com.paravai.communities.offer.application.command.create;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.offer.application.common.OfferMetrics;
import com.paravai.communities.offer.application.event.OfferEventFactory;
import com.paravai.communities.offer.application.snapshot.OfferSnapshotSupport;
import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.domain.model.OfferFactory;
import com.paravai.communities.offer.domain.repository.OfferRepository;
import com.paravai.communities.offer.domain.value.ExchangeTypeValue;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.event.NonBlockingEventPublisher;
import com.paravai.foundation.domain.event.ReactiveDomainEventPublisher;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.OperationTypeValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class CreateOfferService {

    private static final Logger log = LoggerFactory.getLogger(CreateOfferService.class);

    private final OfferRepository offerRepository;
    private final NonBlockingEventPublisher publisher;
    private final OfferSnapshotSupport snapshots;
    private final OfferEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public CreateOfferService(
            OfferRepository offerRepository,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Offer> snapshotMapper,
            OfferEventFactory eventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.offerRepository = Objects.requireNonNull(offerRepository, "offerRepository");
        this.publisher = new NonBlockingEventPublisher(Objects.requireNonNull(domainEventPublisher), log);
        this.snapshots = new OfferSnapshotSupport(Objects.requireNonNull(snapshotMapper));
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    /**
     * Creates a new ACTIVE offer once all cross-module validations
     * have already been performed by an external orchestrator.
     *
     * Covered here:
     * - no duplicate ACTIVE offer for (tenantId, communityId, resourceId)
     * - Offer aggregate creation
     * - persistence
     * - OfferPublished event emission
     *
     * NOT covered here:
     * - community existence
     * - membership ACTIVE validation
     * - resource ownership
     * - community rules / allowed exchange types
     */
    public Mono<CreateOfferResult> create(
            IdValue tenantId,
            IdValue communityId,
            IdValue resourceId,
            IdValue ownerId,
            String exchangeTypeCode,
            String description
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(resourceId, "resourceId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");
        Objects.requireNonNull(exchangeTypeCode, "exchangeTypeCode is required");

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            final OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    OfferMetrics.ID.app("createOffer"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {
                final ExchangeTypeValue exchangeType = ExchangeTypeValue.of(exchangeTypeCode);

                return offerRepository.findActiveByTenantIdAndCommunityIdAndResourceId(
                                tenantId,
                                communityId,
                                resourceId
                        )
                        .flatMap(existing -> Mono.<CreateOfferResult>error(
                                new IllegalArgumentException(
                                        "An ACTIVE offer already exists for this resource in the community"
                                )
                        ))
                        .switchIfEmpty(Mono.defer(() -> {
                            Offer offer = OfferFactory.create(
                                    tenantId,
                                    communityId,
                                    resourceId,
                                    ownerId,
                                    exchangeType,
                                    description
                            );

                            return offerRepository.save(offer)
                                    .flatMap(saved -> publishOfferCreatedEvent(
                                            saved,
                                            traceId,
                                            userOid,
                                            sourceSystem
                                    ).thenReturn(CreateOfferResult.created(saved)));
                        }))
                        .doOnError(ex -> log.error(
                                "[{}][{}] Failed to create offer for resource {} in community {}",
                                traceId,
                                userOid,
                                resourceId,
                                communityId,
                                ex
                        ));
            });
        });
    }

    private Mono<Void> publishOfferCreatedEvent(
            Offer saved,
            String traceId,
            String userOid,
            String sourceSystem
    ) {
        JsonNode current = snapshots.snapshot(saved);

        EntityChangedEvent evt = eventFactory.build(
                OperationTypeValue.CREATED,
                saved.id(),
                traceId,
                userOid,
                sourceSystem,
                "Offer published: " + saved.id(),
                null,
                current
        );

        return publisher.publish(evt);
    }
}