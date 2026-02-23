package com.paravai.regulations.standards.application.command.version;

import com.paravai.foundation.domaincore.event.EntityChangedEvent;
import com.paravai.foundation.domaincore.event.NonBlockingEventPublisher;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.domaincore.value.OperationTypeValue;
import com.paravai.foundation.domaincore.value.TimestampValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.snapshot.SnapshotMapper;
import com.paravai.regulations.standards.application.common.*;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.repository.StandardRepository;
import com.paravai.regulations.standards.domain.value.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class AddStandardVersionService {

    private static final Logger log = LoggerFactory.getLogger(AddStandardVersionService.class);

    private final StandardRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final StandardSnapshotSupport snapshots;
    private final StandardEventFactory standardEventFactory;
    private final ReactiveOperationMetrics metrics;

    public AddStandardVersionService(StandardRepository repo,
                                     com.paravai.foundation.domaincore.event.ReactiveDomainEventPublisher eventPublisher,
                                     SnapshotMapper<Standard> snapshotMapper,
                                     StandardEventFactory standardEventFactory,
                                     ReactiveOperationMetrics metrics) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.nonBlockingPublisher = new NonBlockingEventPublisher(eventPublisher, log);
        this.snapshots = new StandardSnapshotSupport(snapshotMapper);
        this.standardEventFactory = Objects.requireNonNull(standardEventFactory, "standardEventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<Standard> addVersion(IdValue standardId,
                                     StandardVersionValue version,
                                     PublicationDateValue publicationDate,
                                     VisibilityStatusValue visibility,
                                     StandardVersionStatusValue status,
                                     String versionDescription) {

        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("addVersion"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (standardId == null) return Mono.error(new IllegalArgumentException("standardId cannot be null"));
                if (version == null) return Mono.error(new IllegalArgumentException("version cannot be null"));
                if (visibility == null) return Mono.error(new IllegalArgumentException("visibility cannot be null"));
                if (status == null) return Mono.error(new IllegalArgumentException("status cannot be null"));

                log.debug("[{}][{}] Adding version '{}' (status={}) to Standard {} (sourceSystem='{}')",
                        traceId, userOid,
                        version.value(), status.getCode(),
                        standardId,
                        sourceSystem);

                return repo.findById(standardId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                "Standard not found: " + standardId.getValue()
                        )))
                        .flatMap(existing -> {
                            final JsonNode prev = snapshots.snapshot(existing);

                            final Standard updated = existing.addVersion(
                                    version,
                                    publicationDate,
                                    visibility,
                                    status,
                                    versionDescription,
                                    TimestampValue.now()
                            );

                            return repo.save(updated)
                                    .flatMap(saved -> {
                                        log.info("[{}][{}] Added version '{}' (status={}) to Standard {}",
                                                traceId, userOid,
                                                version.value(), status, saved.id());

                                        final JsonNode currentState = snapshots.snapshot(saved);

                                        EntityChangedEvent evt = standardEventFactory.build(
                                                OperationTypeValue.UPDATED,
                                                saved.id(),
                                                traceId, userOid, sourceSystem,
                                                buildMessage(OperationTypeValue.UPDATED, saved, version),
                                                prev,
                                                currentState
                                        );

                                        return nonBlockingPublisher.publish(evt)
                                                .thenReturn(saved);
                                    });
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to add version to Standard {}",
                                traceId, userOid, standardId, ex));
            });
        });
    }

    private String buildMessage(OperationTypeValue op, Standard s, StandardVersionValue version) {
        return "Standard " + op.getValue() + ": " + s.id().getValue()
                + " (version=" + version.value() + ")";
    }
}
