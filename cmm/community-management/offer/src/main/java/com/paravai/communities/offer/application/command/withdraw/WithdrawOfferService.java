package com.paravai.communities.offer.application.command.withdraw;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.offer.application.common.OfferMetrics;
import com.paravai.communities.offer.application.event.OfferEventFactory;
import com.paravai.communities.offer.application.exception.OfferNotFoundException;
import com.paravai.communities.offer.application.snapshot.OfferSnapshotSupport;
import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.domain.repository.OfferRepository;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.event.NonBlockingEventPublisher;
import com.paravai.foundation.domain.event.ReactiveDomainEventPublisher;
import com.paravai.foundation.domain.exception.CustomException;
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
public class WithdrawOfferService {

    private static final Logger log = LoggerFactory.getLogger(WithdrawOfferService.class);

    private final OfferRepository offerRepository;
    private final NonBlockingEventPublisher publisher;
    private final OfferSnapshotSupport snapshots;
    private final OfferEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public WithdrawOfferService(
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
     * Withdraws an offer.
     *
     * Covered here:
     * - offer existence
     * - ownership validation (inside aggregate)
     * - ACTIVE/PAUSED -> WITHDRAWN validation (inside aggregate)
     * - locked validation (inside aggregate)
     * - idempotency by domain
     * - persistence
     * - OfferWithdrawn event emission when there is an effective change
     *
     * NOT covered here:
     * - external cross-module validations
     */
    public Mono<WithdrawOfferResult> withdraw(
            IdValue offerId,
            IdValue currentUserId
    ) {
        Objects.requireNonNull(offerId, "offerId is required");
        Objects.requireNonNull(currentUserId, "currentUserId is required");

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info("[withdraw-offer-service] Reactor context - traceId={}, userOid={}, sourceSystem={}",
                    traceId, userOid, sourceSystem);

            final OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    OfferMetrics.ID.app("withdrawOffer"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    offerRepository.findById(offerId)
                            .switchIfEmpty(Mono.error(new OfferNotFoundException(
                                    "error.offer.not_found",
                                    new Object[]{offerId.value()}
                            )))
                            .flatMap(existing -> {
                                JsonNode previous = snapshots.snapshot(existing);

                                boolean changed = existing.withdraw(
                                        currentUserId,
                                        null
                                );

                                if (!changed) {
                                    return Mono.just(WithdrawOfferResult.unchanged(existing));
                                }

                                return offerRepository.save(existing)
                                        .flatMap(saved -> publishOfferWithdrawnEvent(
                                                saved,
                                                previous,
                                                traceId,
                                                userOid,
                                                sourceSystem
                                        ).thenReturn(WithdrawOfferResult.updated(saved)));
                            })
                            .doOnError(ex -> {
                                if (ex instanceof CustomException) {
                                    log.warn(
                                            "[{}][{}] Business error while withdrawing offer {}: {}",
                                            traceId,
                                            userOid,
                                            offerId,
                                            ex.getMessage()
                                    );
                                } else {
                                    log.error(
                                            "[{}][{}] Failed to withdraw offer {}",
                                            traceId,
                                            userOid,
                                            offerId,
                                            ex
                                    );
                                }
                            })
            );
        });
    }

    private Mono<Void> publishOfferWithdrawnEvent(
            Offer saved,
            JsonNode previous,
            String traceId,
            String userOid,
            String sourceSystem
    ) {
        JsonNode current = snapshots.snapshot(saved);

        EntityChangedEvent evt = eventFactory.build(
                OperationTypeValue.UPDATED,
                saved.id(),
                traceId,
                userOid,
                sourceSystem,
                "Offer status changed to WITHDRAWN: " + saved.id(),
                previous,
                current
        );

        return publisher.publish(evt);
    }
}