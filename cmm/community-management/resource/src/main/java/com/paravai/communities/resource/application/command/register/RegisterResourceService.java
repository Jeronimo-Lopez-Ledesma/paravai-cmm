package com.paravai.communities.resource.application.command.register;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.resource.application.common.ResourceMetrics;
import com.paravai.communities.resource.application.common.ResourceSnapshotSupport;
import com.paravai.communities.resource.application.event.ResourceEventFactory;
import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.domain.model.ResourceFactory;
import com.paravai.communities.resource.domain.repository.ResourceRepository;
import com.paravai.communities.resource.domain.value.ResourceConditionValue;
import com.paravai.communities.resource.domain.value.ResourceTitleValue;
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

import java.util.Objects;

@Service
public class RegisterResourceService {

    private static final Logger log = LoggerFactory.getLogger(RegisterResourceService.class);

    private final ResourceRepository repo;
    private final NonBlockingEventPublisher publisher;
    private final ResourceSnapshotSupport snapshots;
    private final ResourceEventFactory eventFactory;
    private final ReactiveOperationMetrics metrics;

    public RegisterResourceService(
            ResourceRepository repo,
            ReactiveDomainEventPublisher domainEventPublisher,
            SnapshotMapper<Resource> snapshotMapper,
            ResourceEventFactory eventFactory,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.publisher = new NonBlockingEventPublisher(Objects.requireNonNull(domainEventPublisher), log);
        this.snapshots = new ResourceSnapshotSupport(Objects.requireNonNull(snapshotMapper));
        this.eventFactory = Objects.requireNonNull(eventFactory, "eventFactory");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Mono<RegisterResourceResult> register(
            IdValue tenantId,
            IdValue ownerId,
            String title,
            String description,
            String conditionCode
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");
        Objects.requireNonNull(title, "title is required");

        return Mono.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    ResourceMetrics.ID.app("registerResource"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {
                ResourceTitleValue resourceTitle = ResourceTitleValue.of(title);
                ResourceConditionValue condition = parseCondition(conditionCode);

                Resource resource = ResourceFactory.create(
                        tenantId,
                        ownerId,
                        resourceTitle,
                        description,
                        condition
                );

                return repo.save(resource)
                        .flatMap(saved -> publishRegisteredEvent(
                                saved,
                                traceId,
                                userOid,
                                sourceSystem
                        ).thenReturn(RegisterResourceResult.created(saved)))
                        .doOnError(ex -> log.error(
                                "[{}][{}] Failed to register resource for owner {}",
                                traceId,
                                userOid,
                                ownerId,
                                ex
                        ));
            });
        });
    }

    private ResourceConditionValue parseCondition(String conditionCode) {
        if (conditionCode == null || conditionCode.isBlank()) {
            return null;
        }
        return ResourceConditionValue.of(conditionCode);
    }

    private Mono<Void> publishRegisteredEvent(
            Resource saved,
            String traceId,
            String userOid,
            String sourceSystem
    ) {
        JsonNode current = snapshots.snapshot(saved);

        EntityChangedEvent evt = eventFactory.build(
                OperationTypeValue.CREATED,
                saved.id(),
                traceId,
                userOid,
                sourceSystem,
                "Resource registered: " + saved.id(),
                null,
                current
        );

        return publisher.publish(evt);
    }
}