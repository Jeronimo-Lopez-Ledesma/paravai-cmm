package com.paravai.communities.community.application.command.create;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.community.application.common.CommunityMetrics;
import com.paravai.communities.community.application.event.CommunityEventFactory;
import com.paravai.communities.community.application.snapshot.CommunitySnapshotSupport;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.repository.CommunityRepository;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.model.MembershipFactory;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.event.NonBlockingEventPublisher;
import com.paravai.foundation.domain.event.ReactiveDomainEventPublisher;
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
public class CreateCommunityService {

    private static final Logger log = LoggerFactory.getLogger(CreateCommunityService.class);

    private final CommunityRepository communityRepo;
    private final MembershipRepository membershipRepo;

    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final CommunitySnapshotSupport snapshots;
    private final CommunityEventFactory communityEventFactory;
    private final ReactiveOperationMetrics metrics;

    public CreateCommunityService(
            CommunityRepository communityRepo,
            MembershipRepository membershipRepo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Community> snapshotMapper,
            CommunityEventFactory communityEventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.communityRepo = Objects.requireNonNull(communityRepo, "communityRepo");
        this.membershipRepo = Objects.requireNonNull(membershipRepo, "membershipRepo");

        this.nonBlockingPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(domainEventPublisher, "domainEventPublisher"),
                log
        );

        this.snapshots = new CommunitySnapshotSupport(
                Objects.requireNonNull(snapshotMapper, "snapshotMapper")
        );

        this.communityEventFactory = Objects.requireNonNull(communityEventFactory, "communityEventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    /**
     * EPIC A / A1
     * - Persists the Community
     * - Creates the initial ADMIN membership for the creator (A1 AC2)
     * - Emits EntityChangedEvent(CREATED) for Community
     *
     * Notes:
     * - We do NOT call authorization here (creator identity comes from context and is implicit).
     * - No cross-aggregate transaction guarantee unless Mongo transactions are enabled.
     */
    public Mono<Community> create(Community toCreate) {
        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    CommunityMetrics.ID.app("create"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (toCreate == null) {
                    return Mono.error(new IllegalArgumentException("Community cannot be null"));
                }

                log.debug("[{}][{}] Persisting new Community (tenantId='{}', slug='{}', sourceSystem='{}')",
                        traceId, userOid,
                        safeValue(() -> toCreate.tenantId().value()),
                        safeValue(toCreate::slug),
                        sourceSystem);

                return communityRepo.save(toCreate)
                        .flatMap(savedCommunity -> {

                            log.info("[{}][{}] Community {} created", traceId, userOid, savedCommunity.id());

                            // A1 AC2: creator becomes initial ADMIN membership
                            Membership adminMembership = MembershipFactory.createAdmin(
                                    savedCommunity.tenantId(),
                                    savedCommunity.id(),
                                    savedCommunity.createdBy()
                            );

                            return membershipRepo.save(adminMembership)
                                    .doOnSuccess(m -> log.info(
                                            "[{}][{}] Initial membership {} created (role={}, status={}) for community {}",
                                            traceId, userOid,
                                            safeValue(() -> m.id().value()),
                                            safeValue(() -> m.role().getCode()),
                                            safeValue(() -> m.status().getCode()),
                                            savedCommunity.id()
                                    ))
                                    .thenReturn(savedCommunity);
                        })
                        .flatMap(savedCommunity -> {
                            // Event for Community (EntityChangedEvent)
                            final JsonNode currentState = snapshots.snapshot(savedCommunity);

                            EntityChangedEvent evt = communityEventFactory.build(
                                    OperationTypeValue.CREATED,
                                    savedCommunity.id(),
                                    traceId, userOid, sourceSystem,
                                    buildMessage(OperationTypeValue.CREATED, savedCommunity),
                                    null,
                                    currentState
                            );

                            return nonBlockingPublisher.publish(evt).thenReturn(savedCommunity);
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to create Community {}",
                                traceId, userOid, safeId(toCreate), ex));
            });
        });
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    private String buildMessage(OperationTypeValue op, Community c) {
        return "Community " + op.getValue() + ": " + (c != null && c.id() != null ? c.id().value() : "no-id");
    }

    private static String safeId(Community c) {
        try {
            return (c != null && c.id() != null) ? c.id().toString() : "no-id";
        } catch (Exception ignored) {
            return "no-id";
        }
    }

    private static String safeValue(java.util.concurrent.Callable<String> supplier) {
        try {
            String v = supplier.call();
            return (v == null ? "null" : v);
        } catch (Exception ignored) {
            return "n/a";
        }
    }
}