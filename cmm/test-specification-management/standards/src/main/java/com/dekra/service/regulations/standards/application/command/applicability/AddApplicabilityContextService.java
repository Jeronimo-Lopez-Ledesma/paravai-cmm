package com.paravai.regulations.standards.application.command.applicability;

import com.paravai.foundation.domaincore.event.EntityChangedEvent;
import com.paravai.foundation.domaincore.event.NonBlockingEventPublisher;
import com.paravai.foundation.domaincore.value.DateValue;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.domaincore.value.OperationTypeValue;
import com.paravai.foundation.domaincore.value.TimestampValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.snapshot.SnapshotMapper;
import com.paravai.regulations.standards.application.common.StandardEventFactory;
import com.paravai.regulations.standards.application.common.StandardMetrics;
import com.paravai.regulations.standards.application.common.StandardSnapshotSupport;
import com.paravai.regulations.standards.application.exception.StandardNotFoundException;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.repository.StandardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class AddApplicabilityContextService {

    private static final Logger log = LoggerFactory.getLogger(AddApplicabilityContextService.class);

    private final StandardRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final StandardSnapshotSupport snapshots;
    private final StandardEventFactory standardEventFactory;
    private final ReactiveOperationMetrics metrics;

    public AddApplicabilityContextService(StandardRepository repo,
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

    public Mono<Standard> addContext(IdValue standardId,
                                     IdValue versionId,
                                     IdValue certificationSchemeId,
                                     DateValue effectiveDate,
                                     DateValue endOfValidityDate) {

        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("addApplicabilityContext"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (standardId == null) return Mono.error(new IllegalArgumentException("standardId cannot be null"));
                if (versionId == null) return Mono.error(new IllegalArgumentException("versionId cannot be null"));
                if (certificationSchemeId == null) return Mono.error(new IllegalArgumentException("certificationSchemeId cannot be null"));
                if (effectiveDate == null) return Mono.error(new IllegalArgumentException("effectiveDate cannot be null"));
                // endOfValidityDate optional

                log.debug("[{}][{}] Adding applicability context (schemeId={}, effective={}, end={}) to Standard {} version {} (sourceSystem='{}')",
                        traceId, userOid,
                        certificationSchemeId, effectiveDate, endOfValidityDate,
                        standardId, versionId,
                        sourceSystem);

                return repo.findById(standardId)
                        .switchIfEmpty(Mono.error(new StandardNotFoundException(standardId)))

                        .flatMap(existing -> {
                            final JsonNode prev = snapshots.snapshot(existing);

                            final Standard updated = existing.addApplicabilityContext(
                                    versionId,
                                    certificationSchemeId,
                                    effectiveDate,
                                    endOfValidityDate,
                                    TimestampValue.now()
                            );

                            return repo.save(updated)
                                    .flatMap(saved -> {
                                        log.info("[{}][{}] Added applicability context (schemeId={}, effective={}) to Standard {} version {}",
                                                traceId, userOid,
                                                certificationSchemeId, effectiveDate,
                                                saved.id(), versionId);

                                        final JsonNode currentState = snapshots.snapshot(saved);

                                        EntityChangedEvent evt = standardEventFactory.build(
                                                OperationTypeValue.UPDATED,
                                                saved.id(),
                                                traceId, userOid, sourceSystem,
                                                buildMessage(OperationTypeValue.UPDATED, saved, versionId, certificationSchemeId, effectiveDate),
                                                prev,
                                                currentState
                                        );

                                        return nonBlockingPublisher.publish(evt)
                                                .thenReturn(saved);
                                    });
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to add applicability context to Standard {} version {}",
                                traceId, userOid, standardId, versionId, ex));
            });
        });
    }

    private String buildMessage(OperationTypeValue op,
                                Standard s,
                                IdValue versionId,
                                IdValue certificationSchemeId,
                                DateValue effectiveDate) {
        return "Standard " + op.getValue() + ": " + s.id().getValue()
                + " (versionId=" + versionId.getValue()
                + ", schemeId=" + certificationSchemeId.getValue()
                + ", effectiveDate=" + effectiveDate.toIsoString()
                + ")";
    }
}
