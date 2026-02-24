package com.paravai.communities.community.application.command.create;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.community.application.common.CommunityMetrics;
import com.paravai.communities.community.application.event.CommunityEventFactory;
import com.paravai.communities.community.application.snapshot.CommunitySnapshotSupport;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.repository.CommunityRepository;
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

    private final CommunityRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final CommunitySnapshotSupport snapshots;
    private final CommunityEventFactory communityEventFactory;
    private final ReactiveOperationMetrics metrics;

    public CreateCommunityService(
            CommunityRepository repo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Community> snapshotMapper,
            CommunityEventFactory communityEventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
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

                return repo.save(toCreate)
                        .flatMap(saved -> {
                            log.info("[{}][{}] Community {} created", traceId, userOid, saved.id());

                            final JsonNode currentState = snapshots.snapshot(saved);

                            EntityChangedEvent evt = communityEventFactory.build(
                                    OperationTypeValue.CREATED,
                                    saved.id(),
                                    traceId, userOid, sourceSystem,
                                    buildMessage(OperationTypeValue.CREATED, saved),
                                    null,
                                    currentState
                            );

                            return nonBlockingPublisher.publish(evt).thenReturn(saved);
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to create Community {}",
                                traceId, userOid, safeId(toCreate), ex));
            });
        });
    }

    private String buildMessage(OperationTypeValue op, Community c) {
        // Keep it stable and easy to search in logs/AuditLog
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