package com.paravai.communities.community.application.command.visibility;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.community.application.common.CommunityMetrics;
import com.paravai.communities.community.application.event.CommunityEventFactory;
import com.paravai.communities.community.application.snapshot.CommunitySnapshotSupport;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.repository.CommunityRepository;
import com.paravai.communities.community.domain.value.CommunityVisibilityValue;
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
public class ChangeCommunityVisibilityService {

    private static final Logger log = LoggerFactory.getLogger(ChangeCommunityVisibilityService.class);

    private final CommunityRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final CommunitySnapshotSupport snapshots;
    private final CommunityEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public ChangeCommunityVisibilityService(
            CommunityRepository repo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Community> snapshotMapper,
            CommunityEventFactory eventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.nonBlockingPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(domainEventPublisher, "domainEventPublisher"),
                log
        );
        this.snapshots = new CommunitySnapshotSupport(Objects.requireNonNull(snapshotMapper, "snapshotMapper"));
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<Community> changeVisibility(IdValue communityId, CommunityVisibilityValue newVisibility) {
        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    CommunityMetrics.ID.app("changeVisibility"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (communityId == null) return Mono.error(new IllegalArgumentException("communityId cannot be null"));
                if (newVisibility == null) return Mono.error(new IllegalArgumentException("newVisibility cannot be null"));

                return repo.findById(communityId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Community not found: " + communityId)))
                        .flatMap(existing -> {

                            // snapshot before
                            final JsonNode previousState = snapshots.snapshot(existing);

                            final String prevVisibility = existing.visibility().getCode();

                            existing.changeVisibility(newVisibility);

                            final boolean changed = !Objects.equals(prevVisibility, existing.visibility().getCode());

                            if (!changed) {
                                log.debug("[{}][{}] Community {} visibility unchanged ({}). No event emitted.",
                                        traceId, userOid, existing.id(), prevVisibility);
                                return repo.save(existing); // still ok; you could skip save, but safe for now
                            }

                            return repo.save(existing)
                                    .flatMap(saved -> {
                                        final JsonNode currentState = snapshots.snapshot(saved);

                                        EntityChangedEvent evt = eventFactory.build(
                                                OperationTypeValue.UPDATED,
                                                saved.id(),
                                                traceId, userOid, sourceSystem,
                                                buildMessage(saved, prevVisibility, saved.visibility().getCode()),
                                                previousState,
                                                currentState
                                        );

                                        return nonBlockingPublisher.publish(evt).thenReturn(saved);
                                    });
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to change visibility for Community {}",
                                traceId, userOid, communityId, ex));
            });
        });
    }

    private String buildMessage(Community c, String from, String to) {
        return "Community visibility changed: id=%s, from=%s, to=%s"
                .formatted(c.id().value(), from, to);
    }
}