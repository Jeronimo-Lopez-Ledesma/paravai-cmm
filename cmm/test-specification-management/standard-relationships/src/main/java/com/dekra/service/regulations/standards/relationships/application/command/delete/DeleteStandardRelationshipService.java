package com.dekra.service.regulations.standards.relationships.application.command.delete;

import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.domaincore.event.NonBlockingEventPublisher;
import com.dekra.service.foundation.domaincore.event.ReactiveDomainEventPublisher;
import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.OperationTypeValue;
import com.dekra.service.foundation.observability.metrics.MetricsSupport;
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import com.dekra.service.foundation.securityutils.reactive.context.RequestContext;
import com.dekra.service.foundation.snapshot.SnapshotMapper;
import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipEventFactory;
import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipMetrics;
import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipSnapshotSupport;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationship;
import com.dekra.service.regulations.standards.relationships.domain.repository.StandardRelationshipRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class DeleteStandardRelationshipService {

    private static final Logger log = LoggerFactory.getLogger(DeleteStandardRelationshipService.class);

    private final StandardRelationshipRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final StandardRelationshipSnapshotSupport snapshots;
    private final StandardRelationshipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public DeleteStandardRelationshipService(StandardRelationshipRepository repo,
                                             ReactiveDomainEventPublisher domainEventPublisher,
                                             SnapshotMapper<StandardRelationship> snapshotMapper,
                                             StandardRelationshipEventFactory eventFactory,
                                             ReactiveOperationMetrics metrics) {

        this.repo = Objects.requireNonNull(repo, "repo");
        this.nonBlockingPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(domainEventPublisher, "domainEventPublisher"),
                log
        );
        this.snapshots = new StandardRelationshipSnapshotSupport(
                Objects.requireNonNull(snapshotMapper, "snapshotMapper")
        );
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    /**
     * Idempotent delete:
     * - if the relationship does not exist, we simply complete (204).
     * - if it exists, we delete and publish an EntityChangedEvent(DELETED) with prev snapshot.
     */
    public Mono<Void> deleteById(IdValue id) {

        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardRelationshipMetrics.ID.app("deleteById"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

                log.debug("[{}][{}] Deleting StandardRelationship {} (sourceSystem='{}')",
                        traceId, userOid, id, sourceSystem);

                return repo.findById(id)
                        .flatMap(existing -> {
                            final JsonNode prev = snapshots.snapshot(existing);

                            return repo.deleteById(id)
                                    .then(Mono.defer(() -> {
                                        log.info("[{}][{}] StandardRelationship {} deleted", traceId, userOid, id);

                                        EntityChangedEvent evt = eventFactory.build(
                                                OperationTypeValue.DELETED,
                                                id,
                                                traceId, userOid, sourceSystem,
                                                buildMessage(OperationTypeValue.DELETED, id),
                                                prev,
                                                null
                                        );

                                        return nonBlockingPublisher.publish(evt);
                                    }));
                        })
                        // If not found -> idempotent delete (no event)
                        .switchIfEmpty(Mono.fromRunnable(() ->
                                log.info("[{}][{}] StandardRelationship {} not found (idempotent delete)",
                                        traceId, userOid, id)
                        ))
                        .then()
                        .doOnError(ex -> log.error("[{}][{}] Failed to delete StandardRelationship {}",
                                traceId, userOid, id, ex));
            });
        });
    }

    private String buildMessage(OperationTypeValue op, IdValue id) {
        return "StandardRelationship " + op.getValue() + ": " + id.getValue();
    }
}
