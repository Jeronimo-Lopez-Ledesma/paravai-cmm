package com.paravai.communities.membership.application.command.invite;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.application.authorization.MembershipAuthorizationService;
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
public class InviteMemberService {

    private static final Logger log = LoggerFactory.getLogger(InviteMemberService.class);

    private final MembershipRepository repo;
    private final MembershipAuthorizationService authorization;
    private final NonBlockingEventPublisher publisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public InviteMemberService(
            MembershipRepository repo,
            MembershipAuthorizationService authorization,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Membership> snapshotMapper,
            MembershipEventFactory eventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo);
        this.authorization = Objects.requireNonNull(authorization);
        this.publisher = new NonBlockingEventPublisher(Objects.requireNonNull(domainEventPublisher), log);
        this.snapshots = new MembershipSnapshotSupport(Objects.requireNonNull(snapshotMapper));
        this.eventFactory = Objects.requireNonNull(eventFactory);
        this.metrics = Objects.requireNonNull(metrics);
    }

    public Mono<Membership> invite(
            IdValue tenantId,
            IdValue communityId,
            IdValue inviterUserId,
            IdValue inviteeUserId
    ) {
        return Mono.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    MembershipMetrics.ID.app("inviteMember"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    authorization.assertAdmin(tenantId, communityId, inviterUserId)
                            .then(findOrCreateInvitation(
                                    tenantId,
                                    communityId,
                                    inviteeUserId,
                                    traceId,
                                    userOid,
                                    sourceSystem
                            ))
            ).doOnError(ex -> log.error(
                    "[{}][{}] Failed to invite user {} to community {}",
                    traceId,
                    userOid,
                    inviteeUserId,
                    communityId,
                    ex
            ));
        });
    }

    private Mono<Membership> findOrCreateInvitation(
            IdValue tenantId,
            IdValue communityId,
            IdValue inviteeUserId,
            String traceId,
            String userOid,
            String sourceSystem
    ) {
        return repo.findByTenantIdAndCommunityIdAndUserId(tenantId, communityId, inviteeUserId)
                .flatMap(existing -> {
                    if (existing.status().isActive()) {
                        return Mono.error(new IllegalStateException("Invitee is already an active member"));
                    }

                    if (existing.status().isPending()) {
                        // Functional idempotency: existing pending invitation/request is returned as-is.
                        return Mono.just(existing);
                    }

                    if (existing.status().isRejected()) {
                        // Current MVP policy: rejected memberships cannot be re-invited automatically.
                        return Mono.error(new IllegalStateException("Invitee has a rejected membership request"));
                    }

                    return Mono.error(new IllegalStateException(
                            "Unsupported membership status: " + existing.status().getCode()
                    ));
                })
                .switchIfEmpty(Mono.defer(() -> createAndPublishInvitation(
                        tenantId,
                        communityId,
                        inviteeUserId,
                        traceId,
                        userOid,
                        sourceSystem
                )));
    }

    private Mono<Membership> createAndPublishInvitation(
            IdValue tenantId,
            IdValue communityId,
            IdValue inviteeUserId,
            String traceId,
            String userOid,
            String sourceSystem
    ) {
        Membership invite = MembershipFactory.createPendingRequest(
                tenantId,
                communityId,
                inviteeUserId
        );

        return repo.save(invite)
                .flatMap(saved -> {
                    JsonNode current = snapshots.snapshot(saved);

                    EntityChangedEvent evt = eventFactory.build(
                            OperationTypeValue.CREATED,
                            saved.id(),
                            traceId,
                            userOid,
                            sourceSystem,
                            "Membership invitation created: " + saved.id(),
                            null,
                            current
                    );

                    return publisher.publish(evt).thenReturn(saved);
                });
    }
}