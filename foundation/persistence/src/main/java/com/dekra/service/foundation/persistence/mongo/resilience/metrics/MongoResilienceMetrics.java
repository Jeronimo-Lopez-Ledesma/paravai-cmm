package com.dekra.service.foundation.persistence.mongo.resilience.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class MongoResilienceMetrics {

    private final MeterRegistry registry;

    // Cache para no recrear Counter/Timer continuamente
    private final Map<String, Counter> successCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> retryCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> retrySkippedCounters = new ConcurrentHashMap<>();

    private final Map<String, Timer> latencyTimers = new ConcurrentHashMap<>();

    public MongoResilienceMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    // ----------------------------------------------------------
    // COUNTERS
    // ----------------------------------------------------------

    public void incrementSuccess(String operation) {
        successCounters
                .computeIfAbsent(operation,
                        op -> registry.counter("mongo_resilience_success", "operation", op))
                .increment();
    }

    public void incrementError(String operation) {
        errorCounters
                .computeIfAbsent(operation,
                        op -> registry.counter("mongo_resilience_error", "operation", op))
                .increment();
    }

    public void incrementRetry(String operation) {
        retryCounters
                .computeIfAbsent(operation,
                        op -> registry.counter("mongo_resilience_retry", "operation", op))
                .increment();
    }

    public void incrementRetrySkipped(String operation) {
        retrySkippedCounters
                .computeIfAbsent(operation,
                        op -> registry.counter("mongo_resilience_retry_skipped", "operation", op))
                .increment();
    }

    // ----------------------------------------------------------
    // LATENCY
    // ----------------------------------------------------------

    public void recordLatency(String operation, long durationNanos) {
        latencyTimers
                .computeIfAbsent(operation,
                        op -> Timer.builder("mongo_resilience_latency")
                                .description("Latency of MongoDB reactive operations")
                                .tag("operation", op)
                                .publishPercentileHistogram()
                                .register(registry)
                )
                .record(durationNanos, TimeUnit.NANOSECONDS);
    }
}
