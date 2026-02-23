package com.dekra.service.regulations.standards.application.query.search;

import com.dekra.service.foundation.domaincore.exception.ServiceUnavailableException;
import com.dekra.service.foundation.observability.metrics.MetricsSupport;
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import com.dekra.service.foundation.securityutils.reactive.context.RequestContext;
import com.dekra.service.foundation.viewjsonapi.query.FilterSetValue;
import com.dekra.service.foundation.viewjsonapi.query.SearchQueryValue;

import com.dekra.service.regulations.standards.application.common.StandardMetrics;
import com.dekra.service.regulations.standards.domain.model.Standard;
import com.dekra.service.regulations.standards.domain.repository.StandardRepository;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class SearchStandardsService {

    private static final Logger log = LoggerFactory.getLogger(SearchStandardsService.class);

    private final StandardRepository repository;
    private final ReactiveOperationMetrics metrics;

    public SearchStandardsService(StandardRepository repository,
                                  ReactiveOperationMetrics metrics) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    /**
     * Search Standards using ServiceFoundation SearchQueryValue (filters + search + sort + pagination).
     * Expected filters (convention): authority, code, title, visibility, type, version
     */
    public Flux<Standard> search(SearchQueryValue query) {
        return Flux.deferContextual(ctxView -> {
            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("search"),
                    sourceSystem
            );
            return MetricsSupport.timedFlux(metrics, opCtx, () -> {
                if (query == null) return Flux.error(new IllegalArgumentException("query cannot be null"));

                log.debug(
                        "[{}][{}] Searching Standards filters={}, search={}, sort={}, page={}, size={}",
                        traceId, userOid,
                        query.filters() != null ? query.filters().keys() : FilterSetValue.empty().keys(),
                        query.search(), query.sort(),
                        query.page().getPage(), query.page().getSize()
                );

                return repository.search(query)
                        .onErrorMap(this::mapToServiceUnavailable)
                        .doOnSubscribe(s -> log.debug("[{}][{}] Started Standards search", traceId, userOid))
                        .doOnComplete(() -> log.debug("[{}][{}] Finished Standards search", traceId, userOid))
                        .doOnError(e -> log.error("[{}][{}] Error while searching Standards",
                                traceId, userOid, e));
            });
        });
    }

    /**
     * Count Standards matching the same search criteria (useful for pagination metadata).
     */
    public Mono<Long> count(SearchQueryValue query) {
        return Mono.deferContextual(ctxView -> {
            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("count"),
                   sourceSystem
            );
            return MetricsSupport.timedMono(metrics, opCtx, () -> {
                if (query == null) return Mono.error(new IllegalArgumentException("query cannot be null"));

                log.debug(
                        "[{}][{}] Counting Standards filters={}, search={}",
                        traceId, userOid,
                        query.filters() != null ? query.filters().keys() : FilterSetValue.empty().keys(),
                        query.search()
                );

                return repository.count(query)
                        .onErrorMap(this::mapToServiceUnavailable)
                        .doOnSuccess(c -> log.debug("[{}][{}] Total Standards found: {}",
                               traceId, userOid, c))
                        .doOnError(e -> log.error("[{}][{}] Error while counting Standards",
                                traceId, userOid, e));
            });
        });
    }

    private Throwable mapToServiceUnavailable(Throwable e) {
        if (e instanceof MongoException || e instanceof DataAccessException) {
            return new ServiceUnavailableException("error.database.unavailable", e);
        }
        return e;
    }
}
