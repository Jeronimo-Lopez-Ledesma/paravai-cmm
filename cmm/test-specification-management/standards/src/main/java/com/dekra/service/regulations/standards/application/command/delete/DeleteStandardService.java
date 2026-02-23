package com.paravai.regulations.standards.application.command.delete;

import com.paravai.foundation.domaincore.event.EntityChangedEvent;
import com.paravai.foundation.domaincore.event.NonBlockingEventPublisher;
import com.paravai.foundation.domaincore.event.ReactiveDomainEventPublisher;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.domaincore.value.OperationTypeValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.snapshot.SnapshotMapper;
import com.paravai.regulations.standards.application.common.*;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.repository.StandardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class DeleteStandardService {

    private static final Logger log = LoggerFactory.getLogger(DeleteStandardService.class);

    private final StandardRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final StandardSnapshotSupport snapshots;
    private final StandardEventFactory standardEventFactory;
    private final ReactiveOperationMetrics metrics;

    public DeleteStandardService(StandardRepository repo,
                                 ReactiveDomainEventPublisher eventPublisher,
                                 SnapshotMapper<Standard> snapshotMapper,
                                 StandardEventFactory standardEventFactory,
                                 ReactiveOperationMetrics metrics) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.nonBlockingPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(eventPublisher, "eventPublisher"),
                log
        );
        this.snapshots = new StandardSnapshotSupport(snapshotMapper);
        this.standardEventFactory = Objects.requireNonNull(standardEventFactory, "standardEventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<Void> delete(IdValue id) {
        return Mono.deferContextual(ctxView -> {
            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("delete"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {
                if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

                log.debug("[{}][{}] Deleting Standard {} (sourceSystem='{}')",
                        traceId, userOid, id, sourceSystem);

                return repo.findById(id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Standard not found: " + id.getValue())))
                        .flatMap(existing -> {
                            final JsonNode prev = snapshots.snapshot(existing);

                            return repo.deleteById(id)
                                    .then(Mono.defer(() -> {
                                        log.info("[{}][{}] Standard {} deleted",
                                                traceId, userOid, id);

                                        EntityChangedEvent evt = standardEventFactory.build(
                                                OperationTypeValue.DELETED,
                                                existing.id(),
                                                traceId,userOid,sourceSystem,
                                                buildMessage(OperationTypeValue.DELETED, existing),
                                                prev,
                                                null
                                        );

                                        return nonBlockingPublisher.publish(evt);
                                    }));
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to delete Standard {}",
                               traceId, userOid, id, ex));
            });
        });
    }

    private String buildMessage(OperationTypeValue op, Standard s) {
        return "Standard " + op.getValue() + ": " + s.id().getValue();
    }
}
