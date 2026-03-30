package com.paravai.communities.membership.application.command.approve;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.application.authorization.MembershipAuthorizationService;
import com.paravai.communities.membership.application.common.MembershipMetrics;
import com.paravai.communities.membership.application.common.MembershipSnapshotSupport;
import com.paravai.communities.membership.application.event.MembershipEventFactory;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.communities.membership.domain.value.CommunityRoleValue;
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
public class ApproveMembershipService {

    private static final Logger log = LoggerFactory.getLogger(ApproveMembershipService.class);

    private final MembershipRepository repo;
    private final MembershipAuthorizationService authorization;
    private final NonBlockingEventPublisher publisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public ApproveMembershipService(
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

    public Mono<ApproveMembershipResult> approve(
            IdValue tenantId,
            IdValue communityId,
            IdValue approverUserId,
            IdValue membershipId
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(approverUserId, "approverUserId is required");
        Objects.requireNonNull(membershipId, "membershipId is required");

        return Mono.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    MembershipMetrics.ID.app("approveMembership"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    authorization.assertAdmin(tenantId, communityId, approverUserId)
                            .then(repo.findById(membershipId))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Membership not found")))
                            .flatMap(existing -> {
                                if (!existing.communityId().equals(communityId)) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Membership does not belong to the specified community"
                                    ));
                                }

                                if (existing.isActive()) {
                                    return Mono.just(ApproveMembershipResult.unchanged(existing));
                                }

                                if (!existing.isPending()) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Only PENDING memberships can be approved"
                                    ));
                                }

                                JsonNode previous = snapshots.snapshot(existing);

                                boolean changed = existing.approve(
                                        CommunityRoleValue.MEMBER,
                                        TimestampValue.now()
                                );

                                if (!changed) {
                                    return Mono.just(ApproveMembershipResult.unchanged(existing));
                                }

                                return repo.save(existing)
                                        .flatMap(saved -> publishApprovalEvent(
                                                saved,
                                                previous,
                                                traceId,
                                                userOid,
                                                sourceSystem
                                        ).thenReturn(ApproveMembershipResult.changed(saved)));
                            })
                            .doOnError(ex -> log.error(
                                    "[{}][{}] Failed to approve membership {} in community {}",
                                    traceId,
                                    userOid,
                                    membershipId,
                                    communityId,
                                    ex
                            ))
            );
        });
    }

    private Mono<Void> publishApprovalEvent(
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
                "Membership approved: " + saved.id(),
                previous,
                current
        );

        return publisher.publish(evt);
    }
}