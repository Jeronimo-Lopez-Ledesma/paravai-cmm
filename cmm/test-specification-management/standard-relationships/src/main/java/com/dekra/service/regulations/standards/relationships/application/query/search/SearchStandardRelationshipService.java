package com.paravai.regulations.standards.relationships.application.query.search;

import com.paravai.foundation.domaincore.exception.ServiceUnavailableException;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.viewjsonapi.query.FilterSetValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import com.paravai.regulations.standards.relationships.application.common.StandardRelationshipMetrics;
import com.paravai.regulations.standards.relationships.domain.model.StandardRelationship;
import com.paravai.regulations.standards.relationships.domain.repository.StandardRelationshipRepository;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class SearchStandardRelationshipService {

    private static final Logger log = LoggerFactory.getLogger(SearchStandardRelationshipService.class);

    private final StandardRelationshipRepository repository;
    private final ReactiveOperationMetrics metrics;

    public SearchStandardRelationshipService(StandardRelationshipRepository repository,
                                              ReactiveOperationMetrics metrics) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    /**
     * Search StandardRelationships using ServiceFoundation SearchQueryValue (filters + search + sort + pagination).
     *
     * Expected filters (convention):
     * - fromStandardId
     * - fromVersionId
     * - toStandardId
     * - toVersionId
     * - type
     * - purpose
     */
    public Flux<StandardRelationship> search(SearchQueryValue query) {
        return Flux.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardRelationshipMetrics.ID.app("search"),
                    sourceSystem
            );

            return MetricsSupport.timedFlux(metrics, opCtx, () -> {

                if (query == null) return Flux.error(new IllegalArgumentException("query cannot be null"));

                log.debug(
                        "[{}][{}] Searching StandardRelationships filters={}, search={}, sort={}, page={}, size={}",
                        traceId, userOid,
                        query.filters() != null ? query.filters().keys() : FilterSetValue.empty().keys(),
                        query.search(), query.sort(),
                        query.page().getPage(), query.page().getSize()
                );

                return repository.search(query)
                        .onErrorMap(this::mapToServiceUnavailable)
                        .doOnSubscribe(s -> log.debug("[{}][{}] Started StandardRelationships search", traceId, userOid))
                        .doOnComplete(() -> log.debug("[{}][{}] Finished StandardRelationships search", traceId, userOid))
                        .doOnError(e -> log.error("[{}][{}] Error while searching StandardRelationships",
                                traceId, userOid, e));
            });
        });
    }

    /**
     * Count StandardRelationships matching the same search criteria (useful for pagination metadata).
     */
    public Mono<Long> count(SearchQueryValue query) {
        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardRelationshipMetrics.ID.app("count"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {

                if (query == null) return Mono.error(new IllegalArgumentException("query cannot be null"));

                log.debug(
                        "[{}][{}] Counting StandardRelationships filters={}, search={}",
                        traceId, userOid,
                        query.filters() != null ? query.filters().keys() : FilterSetValue.empty().keys(),
                        query.search()
                );

                return repository.count(query)
                        .onErrorMap(this::mapToServiceUnavailable)
                        .doOnSuccess(c -> log.debug("[{}][{}] Total StandardRelationships found: {}",
                                traceId, userOid, c))
                        .doOnError(e -> log.error("[{}][{}] Error while counting StandardRelationships",
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
