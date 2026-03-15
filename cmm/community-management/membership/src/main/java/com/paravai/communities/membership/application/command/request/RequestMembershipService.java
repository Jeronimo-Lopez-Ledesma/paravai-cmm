package com.paravai.communities.membership.application.command.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.application.common.MembershipMetrics;
import com.paravai.communities.membership.application.common.MembershipSnapshotSupport;
import com.paravai.communities.membership.application.event.MembershipEventFactory;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.model.MembershipFactory;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
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
public class RequestMembershipService {

    private static final Logger log = LoggerFactory.getLogger(RequestMembershipService.class);

    private final MembershipRepository repo;
    private final NonBlockingEventPublisher publisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public RequestMembershipService(
            MembershipRepository repo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Membership> snapshotMapper,
            MembershipEventFactory eventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.publisher = new NonBlockingEventPublisher(Objects.requireNonNull(domainEventPublisher), log);
        this.snapshots = new MembershipSnapshotSupport(Objects.requireNonNull(snapshotMapper));
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<RequestMembershipResult> request(
            IdValue tenantId,
            IdValue communityId,
            IdValue userId
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");

        return Mono.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    MembershipMetrics.ID.app("requestMembership"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    repo.findByTenantIdAndCommunityIdAndUserId(tenantId, communityId, userId)
                            .flatMap(existing -> {
                                if (existing.status().isPending()) {
                                    return Mono.just(RequestMembershipResult.existing(existing));
                                }

                                if (existing.status().isActive()) {
                                    return Mono.error(new IllegalArgumentException("User is already an active member"));
                                }

                                if (existing.status().isRejected()) {
                                    return Mono.error(new IllegalArgumentException("User has a rejected membership request"));
                                }

                                return Mono.error(new IllegalStateException(
                                        "Unsupported membership status: " + existing.status().getCode()
                                ));
                            })
                            .switchIfEmpty(Mono.defer(() -> createAndPublishPendingMembership(
                                    tenantId,
                                    communityId,
                                    userId,
                                    traceId,
                                    userOid,
                                    sourceSystem
                            )))
                            .doOnError(ex -> log.error(
                                    "[{}][{}] Failed to request membership for community {}",
                                    traceId,
                                    userOid,
                                    communityId,
                                    ex
                            ))
            );
        });
    }

    private Mono<RequestMembershipResult> createAndPublishPendingMembership(
            IdValue tenantId,
            IdValue communityId,
            IdValue userId,
            String traceId,
            String userOid,
            String sourceSystem
    ) {
        Membership membership = MembershipFactory.createPendingRequest(
                tenantId,
                communityId,
                userId
        );

        return repo.save(membership)
                .flatMap(saved -> {
                    JsonNode current = snapshots.snapshot(saved);

                    EntityChangedEvent evt = eventFactory.build(
                            OperationTypeValue.CREATED,
                            saved.id(),
                            traceId,
                            userOid,
                            sourceSystem,
                            "Membership requested: " + saved.id(),
                            null,
                            current
                    );

                    return publisher.publish(evt)
                            .thenReturn(RequestMembershipResult.created(saved));
                });
    }
}