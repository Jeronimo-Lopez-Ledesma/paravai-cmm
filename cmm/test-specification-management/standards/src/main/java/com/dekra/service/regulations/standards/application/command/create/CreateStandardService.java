package com.dekra.service.regulations.standards.application.command.create;

import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.domaincore.event.NonBlockingEventPublisher;
import com.dekra.service.foundation.domaincore.event.ReactiveDomainEventPublisher;
import com.dekra.service.foundation.domaincore.value.OperationTypeValue;
import com.dekra.service.foundation.observability.metrics.MetricsSupport;
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import com.dekra.service.foundation.securityutils.reactive.context.RequestContext;
import com.dekra.service.foundation.snapshot.SnapshotMapper;
import com.dekra.service.regulations.standards.application.common.*;
import com.dekra.service.regulations.standards.domain.model.Standard;
import com.dekra.service.regulations.standards.domain.repository.StandardRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class CreateStandardService {

    private static final Logger log = LoggerFactory.getLogger(CreateStandardService.class);

    private final StandardRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final StandardSnapshotSupport snapshots;
    private final StandardEventFactory standardEventFactory;
    private final ReactiveOperationMetrics metrics;


    public CreateStandardService(
            StandardRepository repo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Standard> snapshotMapper,
            StandardEventFactory standardEventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.nonBlockingPublisher = new NonBlockingEventPublisher(
                Objects.requireNonNull(domainEventPublisher, "domainEventPublisher"),
                log
        );
        this.snapshots = new StandardSnapshotSupport(Objects.requireNonNull(snapshotMapper, "snapshotMapper"));
        this.standardEventFactory = Objects.requireNonNull(standardEventFactory, "standardEventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<Standard> create(Standard toCreate) {
        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("create"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {
                if (toCreate == null) {
                    return Mono.error(new IllegalArgumentException("Standard cannot be null"));
                }

                log.debug("[{}][{}] Persisting new Standard (code='{}', sourceSystem='{}')",
                        traceId, userOid,
                        toCreate.code().value(),
                        sourceSystem);

                return repo.save(toCreate)
                        .flatMap(saved -> {
                            log.info("[{}][{}] Standard {} created", traceId, userOid, saved.id());

                            final JsonNode currentState = snapshots.snapshot(saved);

                            EntityChangedEvent evt = standardEventFactory.build(
                                    OperationTypeValue.CREATED,
                                    saved.id(),
                                    traceId,userOid,sourceSystem,
                                    buildMessage(OperationTypeValue.CREATED, saved),
                                    null,
                                    currentState
                            );

                            return nonBlockingPublisher.publish(evt).thenReturn(saved);
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to create Standard {}",
                                traceId, userOid, safeId(toCreate), ex));
            });
        });
    }


    private String buildMessage(OperationTypeValue op, Standard s) {
        return "Standard " + op.getValue() + ": " + s.id().getValue();
    }

    private static String safeId(Standard s) {
        try {
            return (s != null && s.id() != null) ? s.id().toString() : "no-id";
        } catch (Exception ignored) {
            return "no-id";
        }
    }
}
