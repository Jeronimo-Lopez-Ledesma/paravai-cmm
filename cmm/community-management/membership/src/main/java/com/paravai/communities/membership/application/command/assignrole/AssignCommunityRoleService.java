package com.paravai.communities.membership.application.command.assignrole;

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
public class AssignCommunityRoleService {

    private static final Logger log = LoggerFactory.getLogger(AssignCommunityRoleService.class);

    private final MembershipRepository repo;
    private final MembershipAuthorizationService authorization;
    private final NonBlockingEventPublisher publisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public AssignCommunityRoleService(
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

    public Mono<AssignCommunityRoleResult> assignRole(
            IdValue tenantId,
            IdValue communityId,
            IdValue actingUserId,
            IdValue targetMembershipId,
            CommunityRoleValue newRole
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(actingUserId, "actingUserId is required");
        Objects.requireNonNull(targetMembershipId, "targetMembershipId is required");
        Objects.requireNonNull(newRole, "newRole is required");

        return Mono.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    MembershipMetrics.ID.app("assignCommunityRole"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    authorization.assertAdmin(tenantId, communityId, actingUserId)
                            .then(repo.findById(targetMembershipId))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Membership not found")))
                            .flatMap(existing -> {
                                if (!existing.communityId().equals(communityId)) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Membership does not belong to the specified community"
                                    ));
                                }

                                if (!existing.isActive()) {
                                    return Mono.error(new IllegalArgumentException(
                                            "Only ACTIVE memberships can receive role changes"
                                    ));
                                }

                                if (existing.role().isPresent() && existing.role().get().equals(newRole)) {
                                    return Mono.just(AssignCommunityRoleResult.unchanged(existing));
                                }

                                boolean demotingLastAdmin =
                                        existing.isActiveAdmin()
                                                && CommunityRoleValue.MEMBER.equals(newRole);

                                Mono<Void> invariantCheck = Mono.empty();

                                if (demotingLastAdmin) {
                                    invariantCheck = repo.countActiveAdmins(tenantId, communityId)
                                            .flatMap(count -> {
                                                if (count <= 1) {
                                                    return Mono.error(new IllegalArgumentException(
                                                            "Cannot remove the last ACTIVE ADMIN from the community"
                                                    ));
                                                }
                                                return Mono.empty();
                                            });
                                }

                                return invariantCheck.then(Mono.defer(() -> {
                                    JsonNode previous = snapshots.snapshot(existing);

                                    boolean changed = existing.changeRole(newRole, TimestampValue.now());

                                    if (!changed) {
                                        return Mono.just(AssignCommunityRoleResult.unchanged(existing));
                                    }

                                    return repo.save(existing)
                                            .flatMap(saved -> publishRoleChangedEvent(
                                                    saved,
                                                    previous,
                                                    traceId,
                                                    userOid,
                                                    sourceSystem
                                            ).thenReturn(AssignCommunityRoleResult.changed(saved)));
                                }));
                            })
                            .doOnError(ex -> log.error(
                                    "[{}][{}] Failed to assign role {} to membership {} in community {}",
                                    traceId,
                                    userOid,
                                    newRole,
                                    targetMembershipId,
                                    communityId,
                                    ex
                            ))
            );
        });
    }

    private Mono<Void> publishRoleChangedEvent(
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
                "Membership role changed: " + saved.id(),
                previous,
                current
        );

        return publisher.publish(evt);
    }
}