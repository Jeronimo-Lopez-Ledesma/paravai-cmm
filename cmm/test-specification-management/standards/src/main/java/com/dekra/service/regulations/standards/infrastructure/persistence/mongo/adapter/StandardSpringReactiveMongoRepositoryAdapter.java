package com.dekra.service.regulations.standards.infrastructure.persistence.mongo.adapter;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.observability.metrics.MetricsSupport; // Service Foundation
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import com.dekra.service.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.dekra.service.foundation.viewjsonapi.query.SearchQueryValue;
import com.dekra.service.regulations.standards.application.common.StandardMetrics;
import com.dekra.service.regulations.standards.domain.model.Standard;
import com.dekra.service.regulations.standards.domain.repository.StandardRepository;
import com.dekra.service.regulations.standards.infrastructure.persistence.mongo.document.StandardDocument;
import com.dekra.service.regulations.standards.infrastructure.persistence.mongo.springdata.StandardSpringReactiveMongoRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

@Component
public class StandardSpringReactiveMongoRepositoryAdapter implements StandardRepository {

    private static final String ADAPTER_NAME = "mongo";

    private final StandardSpringReactiveMongoRepository springRepo;
    private final MongoReactiveEntityFilter<Standard, StandardDocument> filter;
    private final ReactiveOperationMetrics metrics;

    public StandardSpringReactiveMongoRepositoryAdapter(StandardSpringReactiveMongoRepository springRepo,
                                                        MongoReactiveEntityFilter<Standard, StandardDocument> filter,
                                                        ReactiveOperationMetrics metrics) {
        this.springRepo = Objects.requireNonNull(springRepo, "springRepo");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    @Override
    public Mono<Standard> save(Standard standard) {
        if (standard == null) return Mono.error(new IllegalArgumentException("standard cannot be null"));

        OperationCtx opCtx = StandardMetrics.ID.outbound(ADAPTER_NAME, "save");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.save(StandardDocument.fromDomain(standard))
                        .map(StandardDocument::toDomain)
                        .onErrorMap(DuplicateKeyException.class, ex ->
                                new IllegalArgumentException("Duplicate Standard business identity (authority+code)", ex)
                        )
        );
    }

    @Override
    public Mono<Standard> findById(IdValue id) {
        if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

        OperationCtx opCtx = StandardMetrics.ID.outbound(ADAPTER_NAME, "findById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findById(id.getValue())
                        .map(StandardDocument::toDomain)
        );
    }

    @Override
    public Mono<Void> deleteById(IdValue id) {
        if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

        OperationCtx opCtx = StandardMetrics.ID.outbound(ADAPTER_NAME, "deleteById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.deleteById(id.getValue())
        );
    }

    @Override
    public Flux<Standard> search(SearchQueryValue q) {
        if (q == null) return Flux.error(new IllegalArgumentException("q cannot be null"));

        OperationCtx opCtx = StandardMetrics.ID.outbound(ADAPTER_NAME, "search");

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

        OperationCtx opCtx = StandardMetrics.ID.outbound(ADAPTER_NAME, "count");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                filter.countByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value())
                )
        );
    }
}