package com.paravai.communities.community.infrastructure.persistence.mongo.adapter;


import com.paravai.communities.community.application.common.CommunityMetrics;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.repository.CommunityRepository;
import com.paravai.communities.community.infrastructure.persistence.mongo.document.CommunityDocument;
import com.paravai.communities.community.infrastructure.persistence.mongo.springdata.CommunitySpringReactiveMongoRepository;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * Infrastructure adapter (Mongo + Spring Data) for CommunityRepository.
 * Implements domain port without leaking Spring/Mongo to the domain.
 */
@Component
public class CommunitySpringReactiveMongoRepositoryAdapter implements CommunityRepository {

    private static final String ADAPTER_NAME = "mongo";

    private final CommunitySpringReactiveMongoRepository springRepo;
    private final MongoReactiveEntityFilter<Community, CommunityDocument> filter;
    private final ReactiveOperationMetrics metrics;

    public CommunitySpringReactiveMongoRepositoryAdapter(CommunitySpringReactiveMongoRepository springRepo,
                                                         MongoReactiveEntityFilter<Community, CommunityDocument> filter,
                                                         ReactiveOperationMetrics metrics) {
        this.springRepo = Objects.requireNonNull(springRepo, "springRepo");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    @Override
    public Mono<Community> save(Community community) {
        if (community == null) return Mono.error(new IllegalArgumentException("community cannot be null"));

        OperationCtx opCtx = CommunityMetrics.ID.outbound(ADAPTER_NAME, "save");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.save(CommunityDocument.fromDomain(community))
                        .map(CommunityDocument::toDomain)
                        .onErrorMap(DuplicateKeyException.class, ex ->
                                new IllegalArgumentException("Duplicate Community business identity (tenantId+slug)", ex)
                        )
        );
    }

    @Override
    public Mono<Community> findById(IdValue id) {
        if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

        OperationCtx opCtx = CommunityMetrics.ID.outbound(ADAPTER_NAME, "findById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findById(id.value())
                        .map(CommunityDocument::toDomain)
        );
    }

    @Override
    public Mono<Void> deleteById(IdValue id) {
        if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

        OperationCtx opCtx = CommunityMetrics.ID.outbound(ADAPTER_NAME, "deleteById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.deleteById(id.value())
        );
    }

    @Override
    public Mono<Boolean> existsByTenantIdAndSlug(IdValue tenantId, String slug) {
        if (tenantId == null) return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        if (slug == null || slug.isBlank()) return Mono.error(new IllegalArgumentException("slug cannot be null or blank"));

        OperationCtx opCtx = CommunityMetrics.ID.outbound(ADAPTER_NAME, "existsByTenantIdAndSlug");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.existsByTenantIdAndSlug(tenantId.value(), slug.trim())
        );
    }

    @Override
    public Flux<Community> search(SearchQueryValue q) {
        if (q == null) return Flux.error(new IllegalArgumentException("q cannot be null"));

        OperationCtx opCtx = CommunityMetrics.ID.outbound(ADAPTER_NAME, "search");

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
        if (q == null) return Mono.error(new IllegalArgumentException("q cannot be null"));

        OperationCtx opCtx = CommunityMetrics.ID.outbound(ADAPTER_NAME, "count");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                filter.countByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value())
                )
        );
    }
}