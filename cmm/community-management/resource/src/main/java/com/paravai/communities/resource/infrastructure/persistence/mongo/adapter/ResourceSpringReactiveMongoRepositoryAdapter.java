package com.paravai.communities.resource.infrastructure.persistence.mongo.adapter;

import com.paravai.communities.resource.application.common.ResourceMetrics;
import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.domain.repository.ResourceRepository;
import com.paravai.communities.resource.infrastructure.persistence.mongo.document.ResourceDocument;
import com.paravai.communities.resource.infrastructure.persistence.mongo.springdata.ResourceSpringReactiveMongoRepository;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * Infrastructure adapter (Mongo + Spring Data) for ResourceRepository.
 * Implements the domain port without leaking Spring/Mongo details into the domain.
 */
@Component
public class ResourceSpringReactiveMongoRepositoryAdapter implements ResourceRepository {

    private static final String ADAPTER_NAME = "mongo";

    private final ResourceSpringReactiveMongoRepository springRepo;
    private final MongoReactiveEntityFilter<Resource, ResourceDocument> filter;
    private final ReactiveOperationMetrics metrics;

    public ResourceSpringReactiveMongoRepositoryAdapter(
            ResourceSpringReactiveMongoRepository springRepo,
            MongoReactiveEntityFilter<Resource, ResourceDocument> filter,
            ReactiveOperationMetrics metrics
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "springRepo");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    @Override
    public Mono<Resource> save(Resource resource) {
        if (resource == null) {
            return Mono.error(new IllegalArgumentException("resource cannot be null"));
        }

        OperationCtx opCtx = ResourceMetrics.ID.outbound(ADAPTER_NAME, "saveResource");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.save(ResourceDocument.fromDomain(resource))
                        .map(ResourceDocument::toDomain)
                        .onErrorMap(DuplicateKeyException.class, ex ->
                                new IllegalArgumentException(
                                        "Duplicate Resource persistence identity",
                                        ex
                                )
                        )
        );
    }

    @Override
    public Mono<Resource> findById(IdValue id) {
        if (id == null) {
            return Mono.error(new IllegalArgumentException("id cannot be null"));
        }

        OperationCtx opCtx = ResourceMetrics.ID.outbound(ADAPTER_NAME, "findResourceById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findById(id.value())
                        .map(ResourceDocument::toDomain)
        );
    }

    @Override
    public Mono<Resource> findByIdAndOwnerId(IdValue id, IdValue ownerId) {
        if (id == null) {
            return Mono.error(new IllegalArgumentException("id cannot be null"));
        }
        if (ownerId == null) {
            return Mono.error(new IllegalArgumentException("ownerId cannot be null"));
        }

        OperationCtx opCtx = ResourceMetrics.ID.outbound(ADAPTER_NAME, "findResourceByIdAndOwnerId");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findByIdAndOwnerId(
                                id.value(),
                                ownerId.value()
                        )
                        .map(ResourceDocument::toDomain)
        );
    }

    @Override
    public Flux<Resource> findByTenantIdAndOwnerId(
            IdValue tenantId,
            IdValue ownerId,
            int page,
            int size
    ) {
        if (tenantId == null) {
            return Flux.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (ownerId == null) {
            return Flux.error(new IllegalArgumentException("ownerId cannot be null"));
        }
        if (page < 1) {
            return Flux.error(new IllegalArgumentException("page must be greater than or equal to 1"));
        }
        if (size <= 0) {
            return Flux.error(new IllegalArgumentException("size must be greater than zero"));
        }

        OperationCtx opCtx = ResourceMetrics.ID.outbound(ADAPTER_NAME, "findResourcesByTenantIdAndOwnerIdPaged");

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                springRepo.findByTenantIdAndOwnerId(
                                tenantId.value(),
                                ownerId.value(),
                                PageRequest.of(page - 1, size)
                        )
                        .map(ResourceDocument::toDomain)
        );
    }

    @Override
    public Mono<Long> countByTenantIdAndOwnerId(
            IdValue tenantId,
            IdValue ownerId
    ) {
        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (ownerId == null) {
            return Mono.error(new IllegalArgumentException("ownerId cannot be null"));
        }

        OperationCtx opCtx = ResourceMetrics.ID.outbound(ADAPTER_NAME, "countResourcesByTenantIdAndOwnerId");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.countByTenantIdAndOwnerId(
                        tenantId.value(),
                        ownerId.value()
                )
        );
    }

    @Override
    public Flux<Resource> search(SearchQueryValue q) {
        if (q == null) {
            return Flux.error(new IllegalArgumentException("q cannot be null"));
        }

        OperationCtx opCtx = ResourceMetrics.ID.outbound(ADAPTER_NAME, "searchResources");

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                filter.findByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value()),
                        q.sort().isEmpty() ? Optional.empty() : Optional.of(q.sort().toString()),
                        q.page().getPage(),
                        q.page().getSize()
                )
        );
    }

    @Override
    public Mono<Long> count(SearchQueryValue q) {
        if (q == null) {
            return Mono.error(new IllegalArgumentException("q cannot be null"));
        }

        OperationCtx opCtx = ResourceMetrics.ID.outbound(ADAPTER_NAME, "countResources");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                filter.countByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value())
                )
        );
    }
}