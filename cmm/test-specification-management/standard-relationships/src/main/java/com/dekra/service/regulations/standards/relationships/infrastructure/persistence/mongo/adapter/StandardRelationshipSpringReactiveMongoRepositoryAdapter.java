package com.dekra.service.regulations.standards.relationships.infrastructure.persistence.mongo.adapter;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.observability.metrics.MetricsSupport;
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import com.dekra.service.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.dekra.service.foundation.viewjsonapi.query.SearchQueryValue;
import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipMetrics;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationship;
import com.dekra.service.regulations.standards.relationships.domain.repository.StandardRelationshipRepository;
import com.dekra.service.regulations.standards.relationships.infrastructure.persistence.mongo.document.StandardRelationshipDocument;
import com.dekra.service.regulations.standards.relationships.infrastructure.persistence.mongo.springdata.StandardRelationshipSpringReactiveMongoRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

@Component
public class StandardRelationshipSpringReactiveMongoRepositoryAdapter implements StandardRelationshipRepository {

    private static final String ADAPTER_NAME = "mongo";

    private final StandardRelationshipSpringReactiveMongoRepository springRepo;
    private final MongoReactiveEntityFilter<StandardRelationship, StandardRelationshipDocument> filter;
    private final ReactiveOperationMetrics metrics;

    public StandardRelationshipSpringReactiveMongoRepositoryAdapter(
            StandardRelationshipSpringReactiveMongoRepository springRepo,
            MongoReactiveEntityFilter<StandardRelationship, StandardRelationshipDocument> filter,
            ReactiveOperationMetrics metrics
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "springRepo");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    @Override
    public Mono<StandardRelationship> save(StandardRelationship relationship) {
        if (relationship == null) return Mono.error(new IllegalArgumentException("relationship cannot be null"));

        OperationCtx opCtx = StandardRelationshipMetrics.ID.outbound(ADAPTER_NAME, "save");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.save(StandardRelationshipDocument.fromDomain(relationship))
                        .map(StandardRelationshipDocument::toDomain)
                        .onErrorMap(DuplicateKeyException.class, ex ->
                                new IllegalArgumentException(
                                        "Duplicate StandardRelationship (from+type+purpose+to) already exists",
                                        ex
                                )
                        )
        );
    }

    @Override
    public Mono<StandardRelationship> findById(IdValue id) {
        if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

        OperationCtx opCtx = StandardRelationshipMetrics.ID.outbound(ADAPTER_NAME, "findById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findById(id.getValue())
                        .map(StandardRelationshipDocument::toDomain)
        );
    }

    @Override
    public Mono<Void> deleteById(IdValue id) {
        if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

        OperationCtx opCtx = StandardRelationshipMetrics.ID.outbound(ADAPTER_NAME, "deleteById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.deleteById(id.getValue())
        );
    }

    @Override
    public Flux<StandardRelationship> search(SearchQueryValue q) {
        if (q == null) return Flux.error(new IllegalArgumentException("q cannot be null"));

        OperationCtx opCtx = StandardRelationshipMetrics.ID.outbound(ADAPTER_NAME, "search");

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

        OperationCtx opCtx = StandardRelationshipMetrics.ID.outbound(ADAPTER_NAME, "count");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                filter.countByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value())
                )
        );
    }
}
