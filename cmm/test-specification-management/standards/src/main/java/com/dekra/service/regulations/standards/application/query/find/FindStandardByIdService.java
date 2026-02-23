package com.paravai.regulations.standards.application.query.find;

import com.paravai.foundation.domaincore.exception.ServiceUnavailableException;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;

import com.paravai.regulations.standards.application.common.StandardMetrics;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.repository.StandardRepository;
import com.mongodb.MongoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class FindStandardByIdService {

    private static final Logger log = LoggerFactory.getLogger(FindStandardByIdService.class);

    private final StandardRepository repository;
    private final ReactiveOperationMetrics metrics;

    public FindStandardByIdService(StandardRepository repository,
                                   ReactiveOperationMetrics metrics) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }


    public Mono<Standard> findById(IdValue id) {
        return Mono.deferContextual(ctxView -> {

            final String traceId = RequestContext.getTraceId(ctxView);
            final String userOid = RequestContext.getUserOid(ctxView);
            final String sourceSystem = RequestContext.getSourceSystem(ctxView);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    StandardMetrics.ID.app("findById"),
                    sourceSystem            );

            return MetricsSupport.timedMono(metrics, opCtx, () -> {
                if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));

                log.debug("[{}][{}] Looking up Standard {}", traceId, userOid, id);

                return repository.findById(id)
                        .doOnNext(s -> log.debug("[{}][{}] Standard {} found", traceId, userOid, id))
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("[{}][{}] Standard {} not found", traceId, userOid, id);
                            return Mono.empty();
                        }))
                        .onErrorMap(this::mapToServiceUnavailable)
                        .doOnError(e -> log.error("[{}][{}] Error retrieving Standard {}",
                                traceId, userOid, id, e));
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
