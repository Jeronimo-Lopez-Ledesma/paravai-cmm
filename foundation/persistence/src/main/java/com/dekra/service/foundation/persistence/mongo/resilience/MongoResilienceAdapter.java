package com.dekra.service.foundation.persistence.mongo.resilience;

import com.dekra.service.foundation.persistence.mongo.resilience.metrics.MongoResilienceMetrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Predicate;

@Component
public class MongoResilienceAdapter {

    private static final Logger log = LoggerFactory.getLogger(MongoResilienceAdapter.class);

    private final MongoCircuitBreakerRegistryFactory cbFactory;
    private final RetryBudgetService retryBudget;
    private final MongoResilienceMetrics metrics;

    public MongoResilienceAdapter(
            MongoCircuitBreakerRegistryFactory cbFactory,
            RetryBudgetService retryBudget,
            MongoResilienceMetrics metrics
    ) {
        this.cbFactory = cbFactory;
        this.retryBudget = retryBudget;
        this.metrics = metrics;
    }

    // -------------------------------------------------------------------------
    // Mongo errors that are retryable
    // -------------------------------------------------------------------------
    private static final Predicate<Throwable> MONGO_RETRYABLE =
            ex -> !(ex instanceof IllegalArgumentException ||
                    ex instanceof UnsupportedOperationException);

    // -------------------------------------------------------------------------
    // MONO PROTECTED EXECUTION (CB + RetryBudget + Metrics + Structured Logging)
    // -------------------------------------------------------------------------
    public <T> Mono<T> protectWithRetries(
            String repository,
            String operation,
            Mono<T> pipeline
    ) {

        String fullName = repository + "." + operation;
        MDC.put("mongoOperation", fullName);

        CircuitBreaker cb =
                cbFactory.getBreaker(repository, operation);

        return pipeline
                // CIRCUIT BREAKER POR OPERACIÓN
                .transformDeferred(CircuitBreakerOperator.of(cb))

                // RETRY CONTROLADO POR BUDGET
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(150))
                                .maxBackoff(Duration.ofSeconds(1))
                                .transientErrors(true)
                                .filter(MONGO_RETRYABLE)
                                .filter(ex -> allowRetryWithBudget(fullName, ex))
                                .doBeforeRetry(rs -> {
                                    metrics.incrementRetry(fullName);
                                    log.warn(
                                            "[MongoResilience][{}] Retry {} due to {}",
                                            fullName,
                                            rs.totalRetries() + 1,
                                            rs.failure().toString()
                                    );
                                })
                )

                // MÉTRICAS
                .doOnSuccess(v -> metrics.incrementSuccess(fullName))
                .doOnError(ex -> metrics.incrementError(fullName))

                // LOGGING ESTRUCTURADO
                .doOnError(ex -> log.error(
                        "[MongoResilience][{}] Operation failed: {}",
                        fullName,
                        ex.toString()
                ))

                .doFinally(sig -> MDC.remove("mongoOperation"));
    }

    // -------------------------------------------------------------------------
    // FLUX PROTECTED EXECUTION
    // -------------------------------------------------------------------------
    public <T> Flux<T> protectWithRetries(
            String repository,
            String operation,
            Flux<T> pipeline
    ) {
        String fullName = repository + "." + operation;
        MDC.put("mongoOperation", fullName);

        CircuitBreaker cb =
                cbFactory.getBreaker(repository, operation);

        return pipeline
                .transformDeferred(CircuitBreakerOperator.of(cb))
                .retryWhen(
                        Retry.backoff(2, Duration.ofMillis(150))
                                .maxBackoff(Duration.ofSeconds(1))
                                .transientErrors(true)
                                .filter(MONGO_RETRYABLE)
                                .filter(ex -> allowRetryWithBudget(fullName, ex))
                                .doBeforeRetry(rs -> metrics.incrementRetry(fullName))
                )
                .doOnComplete(() -> metrics.incrementSuccess(fullName))
                .doOnError(ex -> metrics.incrementError(fullName))
                .doFinally(sig -> MDC.remove("mongoOperation"));
    }

    // -------------------------------------------------------------------------
    // Retry Budget guard
    // -------------------------------------------------------------------------
    private boolean allowRetryWithBudget(String fullName, Throwable ex) {
        boolean allowed = retryBudget.tryConsumeRetry(fullName);

        if (!allowed) {
            log.warn(
                    "[MongoResilience][{}] Retry skipped → budget exhausted. error={}",
                    fullName,
                    ex.toString()
            );
            metrics.incrementRetrySkipped(fullName);
        }

        return allowed;
    }
}
