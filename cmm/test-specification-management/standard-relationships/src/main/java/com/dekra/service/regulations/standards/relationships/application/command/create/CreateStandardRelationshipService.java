package com.dekra.service.regulations.standards.relationships.application.command.create;

import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.domaincore.event.NonBlockingEventPublisher;
import com.dekra.service.foundation.domaincore.event.ReactiveDomainEventPublisher;
import com.dekra.service.foundation.domaincore.value.OperationTypeValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.foundation.observability.metrics.MetricsSupport;
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import com.dekra.service.foundation.securityutils.reactive.context.RequestContext;
import com.dekra.service.foundation.snapshot.SnapshotMapper;
import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipEventFactory;
import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipMetrics;
import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipSnapshotSupport;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationship;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationshipFactory;
import com.dekra.service.regulations.standards.relationships.domain.repository.StandardRelationshipRepository;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardVersionRefValue;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class CreateStandardRelationshipService {

    private static final Logger log = LoggerFactory.getLogger(CreateStandardRelationshipService.class);

    private final StandardRelationshipRepository repo;
    private final NonBlockingEventPublisher nonBlockingPublisher;
    private final StandardRelationshipSnapshotSupport snapshots;
    private final StandardRelationshipEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public CreateStandardRelationshipService(StandardRelationshipRepository repo,
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

    public Mono<StandardRelationship> create(StandardVersionRefValue from,
                                             StandardVersionRefValue to,
                                             StandardRelationshipTypeValue type,
                                             StandardRelationshipPurposeValue purpose) {

        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardRelationshipMetrics.ID.app("create"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (from == null) return Mono.error(new IllegalArgumentException("from cannot be null"));
                if (to == null) return Mono.error(new IllegalArgumentException("to cannot be null"));
                if (type == null) return Mono.error(new IllegalArgumentException("type cannot be null"));
                // purpose can be null depending on type (domain invariant)

                log.debug("[{}][{}] Creating StandardRelationship (type='{}', from='{}', to='{}', sourceSystem='{}')",
                        traceId, userOid,
                        type.getCode(),
                        safeKey(from),
                        safeKey(to),
                        sourceSystem
                );

                final StandardRelationship toCreate = StandardRelationshipFactory.create(
                        from,
                        to,
                        type,
                        purpose,
                        TimestampValue.now()
                );

                return repo.save(toCreate)
                        .flatMap(saved -> {
                            log.info("[{}][{}] StandardRelationship {} created", traceId, userOid, saved.id());

                            final JsonNode currentState = snapshots.snapshot(saved);

                            EntityChangedEvent evt = eventFactory.build(
                                    OperationTypeValue.CREATED,
                                    saved.id(),
                                    traceId, userOid, sourceSystem,
                                    buildMessage(OperationTypeValue.CREATED, saved),
                                    null,
                                    currentState
                            );

                            return nonBlockingPublisher.publish(evt).thenReturn(saved);
                        })
                        .doOnError(ex -> log.error("[{}][{}] Failed to create StandardRelationship (from='{}', to='{}', type='{}')",
                                traceId, userOid, safeKey(from), safeKey(to), safeType(type), ex));
            });
        });
    }

    private String buildMessage(OperationTypeValue op, StandardRelationship r) {
        return "StandardRelationship " + op.getValue() + ": " + r.id().getValue();
    }

    private static String safeKey(StandardVersionRefValue ref) {
        try {
            return ref != null ? ref.key() : "no-ref";
        } catch (Exception ignored) {
            return "no-ref";
        }
    }

    private static String safeType(StandardRelationshipTypeValue type) {
        try {
            return type != null ? type.getCode() : "no-type";
        } catch (Exception ignored) {
            return "no-type";
        }
    }
}
