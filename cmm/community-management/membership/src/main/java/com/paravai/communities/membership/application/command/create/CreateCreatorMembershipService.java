package com.paravai.communities.membership.application.command.create;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.application.common.MembershipEventFactory;
import com.paravai.communities.membership.application.common.MembershipMetrics;
import com.paravai.communities.membership.application.common.MembershipSnapshotSupport;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.model.MembershipFactory;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.communities.membership.domain.value.CommunityRoleValue;
import com.paravai.communities.membership.domain.value.MembershipStatusValue;
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
public class CreateCreatorMembershipService {

    private static final Logger log = LoggerFactory.getLogger(CreateCreatorMembershipService.class);

    private final MembershipRepository repo;
    private final NonBlockingEventPublisher publisher;
    private final MembershipSnapshotSupport snapshots;
    private final MembershipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public CreateCreatorMembershipService(
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

    public Mono<Membership> createAdminForCommunity(IdValue tenantId, IdValue communityId, IdValue userId) {
        return Mono.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    MembershipMetrics.ID.app("createCreatorMembership"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {
                if (tenantId == null || communityId == null || userId == null) {
                    return Mono.error(new IllegalArgumentException("tenantId/communityId/userId cannot be null"));
                }

                // Idempotent: if already exists, return it
                return repo.findByTenantIdAndCommunityIdAndUserId(tenantId, communityId, userId)
                        .switchIfEmpty(Mono.defer(() -> {
                            Instant now = Instant.now();
                            Membership m = MembershipFactory.createAdmin(
                                    tenantId,
                                    communityId,
                                    userId
                            );
                            return repo.save(m);
                        }))
                        .flatMap(saved -> {
                            JsonNode current = snapshots.snapshot(saved);

                            EntityChangedEvent evt = eventFactory.build(
                                    OperationTypeValue.CREATED,
                                    saved.id(),
                                    traceId, userOid, sourceSystem,
                                    "Membership CREATED: " + saved.id().toString(),
                                    null,
                                    current
                            );

                            return publisher.publish(evt).thenReturn(saved);
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to create creator membership for community {}",
                                traceId, userOid, communityId, ex));
            });
        });
    }
}