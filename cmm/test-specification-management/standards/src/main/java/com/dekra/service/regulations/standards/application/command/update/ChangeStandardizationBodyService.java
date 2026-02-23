package com.dekra.service.regulations.standards.application.command.update;


import com.dekra.service.foundation.domain.organization.value.OrganizationAssociationValue;
import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.domaincore.event.NonBlockingEventPublisher;
import com.dekra.service.foundation.domaincore.event.ReactiveDomainEventPublisher;
import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.OperationTypeValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.foundation.observability.metrics.MetricsSupport;
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import com.dekra.service.foundation.securityutils.reactive.context.RequestContext;
import com.dekra.service.foundation.snapshot.SnapshotMapper;
import com.dekra.service.regulations.standards.application.common.StandardEventFactory;
import com.dekra.service.regulations.standards.application.common.StandardMetrics;
import com.dekra.service.regulations.standards.application.common.StandardSnapshotSupport;
import com.dekra.service.regulations.standards.application.exception.StandardNotFoundException;
import com.dekra.service.regulations.standards.domain.model.Standard;
import com.dekra.service.regulations.standards.domain.repository.StandardRepository;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class ChangeStandardizationBodyService {

    private static final Logger log = LoggerFactory.getLogger(ChangeStandardizationBodyService.class);

    private final StandardRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final StandardSnapshotSupport snapshots;
    private final StandardEventFactory standardEventFactory;
    private final ReactiveOperationMetrics metrics;

    public ChangeStandardizationBodyService(StandardRepository repo,
                                            ReactiveDomainEventPublisher domainEventPublisher,
                                            SnapshotMapper<Standard> snapshotMapper,
                                            StandardEventFactory standardEventFactory,
                                            ReactiveOperationMetrics metrics) {

        this.repo = Objects.requireNonNull(repo, "repo");
        this.nonBlockingPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(domainEventPublisher, "domainEventPublisher"),
                log
        );
        this.snapshots = new StandardSnapshotSupport(Objects.requireNonNull(snapshotMapper, "snapshotMapper"));
        this.standardEventFactory = Objects.requireNonNull(standardEventFactory, "standardEventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<Standard> changeStandardizationBody(IdValue id,
                                                    OrganizationAssociationValue standardizationBody) {

        return Mono.deferContextual(ctxView -> {
            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("changeIssuingBody"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));
                if (standardizationBody == null) return Mono.error(new IllegalArgumentException("standardizationBody cannot be null"));

                log.debug("[{}][{}] Changing standardizationBody for Standard {} (sourceSystem='{}')",
                        traceId, userOid, id, sourceSystem);

                return repo.findById(id)
                        .switchIfEmpty(Mono.error(new StandardNotFoundException(id)))
                        .flatMap(existing -> {
                            final JsonNode prev = snapshots.snapshot(existing);

                            // Controlled mutation on the aggregate root:
                            // - standardizationBody is mandatory
                            // - does not affect versions
                            // - preserves immutability (returns new instance)
                            final Standard updated = existing.changeIssuingBody(
                                    standardizationBody,
                                    TimestampValue.now()
                            );

                            return repo.save(updated)
                                    .flatMap(saved -> {
                                        log.info("[{}][{}] Standard {} standardizationBody changed",
                                                traceId, userOid, saved.id());

                                        final JsonNode currentState = snapshots.snapshot(saved);

                                        EntityChangedEvent evt = standardEventFactory.build(
                                                OperationTypeValue.UPDATED,
                                                saved.id(),
                                                traceId, userOid, sourceSystem,
                                                buildMessage(OperationTypeValue.UPDATED, saved),
                                                prev,
                                                currentState
                                        );

                                        return nonBlockingPublisher.publish(evt).thenReturn(saved);
                                    });
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to change standardizationBody for Standard {}",
                                traceId, userOid, id, ex));
            });
        });
    }

    private String buildMessage(OperationTypeValue op, Standard s) {
        return "Standard " + op.getValue() + " (issuingBody changed): " + s.id().getValue();
    }
}