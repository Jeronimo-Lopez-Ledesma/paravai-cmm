package com.paravai.communities.resource.application.query.listmy;

import com.paravai.communities.resource.application.common.ResourceMetrics;
import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.domain.repository.ResourceRepository;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class ListMyResourcesService {

    private static final Logger log = LoggerFactory.getLogger(ListMyResourcesService.class);

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final ResourceRepository repo;
    private final ReactiveOperationMetrics metrics;

    public ListMyResourcesService(
            ResourceRepository repo,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Flux<Resource> list(
            IdValue tenantId,
            IdValue ownerId,
            Integer page,
            Integer size
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);

        return Flux.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    ResourceMetrics.ID.app("listMyResources"),
                    sourceSystem
            );

            return MetricsSupport.timedFlux(metrics, opCtx, () ->
                    repo.findByTenantIdAndOwnerId(
                                    tenantId,
                                    ownerId,
                                    effectivePage,
                                    effectiveSize
                            )
                            .doOnError(ex -> log.error(
                                    "[{}][{}] Failed to list resources for owner {}",
                                    traceId,
                                    userOid,
                                    ownerId,
                                    ex
                            ))
            );
        });
    }

    public Mono<Long> count(
            IdValue tenantId,
            IdValue ownerId
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        return Mono.deferContextual(ctx -> {
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    ResourceMetrics.ID.app("countMyResources"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    repo.countByTenantIdAndOwnerId(tenantId, ownerId)
            );
        });
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than or equal to 1");
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
        if (size > MAX_SIZE) {
            return MAX_SIZE;
        }
        return size;
    }
}