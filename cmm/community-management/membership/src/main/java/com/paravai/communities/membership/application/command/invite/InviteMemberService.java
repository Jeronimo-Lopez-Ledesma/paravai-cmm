package com.paravai.communities.membership.application.command.invite;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.application.authorization.MembershipAuthorizationService;
import com.paravai.communities.membership.application.common.MembershipEventFactory;
import com.paravai.communities.membership.application.common.MembershipMetrics;
import com.paravai.communities.membership.application.common.MembershipSnapshotSupport;
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

import java.time.Instant;
import java.util.Objects;

@Service
public class InviteMemberService {

    private final MembershipRepository repo;
    private final MembershipAuthorizationService authorization;
    private final NonBlockingEventPublisher publisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;
    private static final Logger log = LoggerFactory.getLogger(InviteMemberService.class);

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

    public Mono<Membership> invite(IdValue tenantId, IdValue communityId, IdValue inviterUserId, IdValue inviteeUserId) {
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
                            .then(repo.findByTenantIdAndCommunityIdAndUserId(tenantId, communityId, inviteeUserId))
                            .flatMap(existing -> {
                                if (existing.status().isActive()) {
                                    return Mono.error(new IllegalArgumentException("Invitee already active member"));
                                }
                                if (existing.status().isPending()) {
                                    return Mono.just(existing); // idempotent return existing invite
                                }
                                // REVOKED/INACTIVE -> new invitation (policy choice)
                                Instant now = Instant.now();
                                Membership invite = MembershipFactory.createInvitation(
                                        tenantId,
                                        communityId,
                                        inviteeUserId
                                );
                                return repo.save(invite);
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                Instant now = Instant.now();
                                Membership invite = MembershipFactory.createInvitation(
                                        tenantId,
                                        communityId,
                                        inviteeUserId
                                );
                                return repo.save(invite);
                            }))
                            .flatMap(saved -> {
                                JsonNode current = snapshots.snapshot(saved);

                                EntityChangedEvent evt = eventFactory.build(
                                        OperationTypeValue.CREATED,
                                        saved.id(),
                                        traceId, userOid, sourceSystem,
                                        "Membership INVITED: " + saved.id().toString(),
                                        null,
                                        current
                                );

                                return publisher.publish(evt).thenReturn(saved);
                            })
            );
        });
    }
}