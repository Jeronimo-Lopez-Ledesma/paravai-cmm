package com.paravai.communities.membership.application.command.reject;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.application.authorization.MembershipAuthorizationService;
import com.paravai.communities.membership.application.common.MembershipMetrics;
import com.paravai.communities.membership.application.common.MembershipSnapshotSupport;
import com.paravai.communities.membership.application.event.MembershipEventFactory;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.event.NonBlockingEventPublisher;
import com.paravai.foundation.domain.event.ReactiveDomainEventPublisher;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.OperationTypeValue;
import com.paravai.foundation.domain.value.TimestampValue;
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
public class RejectMembershipService {

    private static final Logger log = LoggerFactory.getLogger(RejectMembershipService.class);

    private final MembershipRepository repo;
    private final MembershipAuthorizationService authorization;
    private final NonBlockingEventPublisher publisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public RejectMembershipService(
            MembershipRepository repo,
            MembershipAuthorizationService authorization,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Membership> snapshotMapper,
            MembershipEventFactory eventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.authorization = Objects.requireNonNull(authorization, "authorization");
        this.publisher = new NonBlockingEventPublisher(Objects.requireNonNull(domainEventPublisher), log);
        this.snapshots = new MembershipSnapshotSupport(Objects.requireNonNull(snapshotMapper));
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<RejectMembershipResult> reject(
            IdValue tenantId,
            IdValue communityId,
            IdValue rejectorUserId,
            IdValue membershipId,
            String reason
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(rejectorUserId, "rejectorUserId is required");
        Objects.requireNonNull(membershipId, "membershipId is required");

        return Mono.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    MembershipMetrics.ID.app("rejectMembership"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    authorization.assertAdmin(tenantId, communityId, rejectorUserId)
                            .then(repo.findById(membershipId))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Membership not found")))
                            .flatMap(existing -> {
                                if (!existing.communityId().equals(communityId)) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Membership does not belong to the specified community"
                                    ));
                                }

                                if (existing.isRejected()) {
                                    return Mono.just(RejectMembershipResult.unchanged(existing));
                                }

                                if (!existing.isPending()) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Only PENDING memberships can be rejected"
                                    ));
                                }

                                JsonNode previous = snapshots.snapshot(existing);

                                boolean changed = existing.reject(reason, TimestampValue.now());

                                if (!changed) {
                                    return Mono.just(RejectMembershipResult.unchanged(existing));
                                }

                                return repo.save(existing)
                                        .flatMap(saved -> publishRejectionEvent(
                                                saved,
                                                previous,
                                                traceId,
                                                userOid,
                                                sourceSystem
                                        ).thenReturn(RejectMembershipResult.changed(saved)));
                            })
                            .doOnError(ex -> log.error(
                                    "[{}][{}] Failed to reject membership {} in community {}",
                                    traceId,
                                    userOid,
                                    membershipId,
                                    communityId,
                                    ex
                            ))
            );
        });
    }

    private Mono<Void> publishRejectionEvent(
            Membership saved,
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
                "Membership rejected: " + saved.id(),
                previous,
                current
        );

        return publisher.publish(evt);
    }
}