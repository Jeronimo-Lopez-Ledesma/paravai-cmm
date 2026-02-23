package com.dekra.service.foundation.observability.metrics;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class MetricsSupport {

    private MetricsSupport() {}

    private static final String TAG_SOURCE_SYSTEM = "sourceSystem";

    // Platform default (no yaml, no allowlist)
    private static final SourceSystemTagNormalizer DEFAULT_NORMALIZER = new SourceSystemTagNormalizer();

    public static <T> Mono<T> timedMono(ReactiveOperationMetrics metrics,
                                        OperationCtx ctx,
                                        Supplier<Mono<T>> pipelineSupplier) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(pipelineSupplier, "pipelineSupplier");

        return metrics.timedMono(ctx, Mono.defer(pipelineSupplier));
    }

    public static <T> Flux<T> timedFlux(ReactiveOperationMetrics metrics,
                                        OperationCtx ctx,
                                        Supplier<Flux<T>> pipelineSupplier) {
        Objects.requireNonNull(metrics, "metrics");
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(pipelineSupplier, "pipelineSupplier");

        return metrics.timedFlux(ctx, Flux.defer(pipelineSupplier));
    }

    /**
     * Returns a new OperationCtx enriched with a low-cardinality "sourceSystem" tag.
     * Normalization is applied implicitly using the platform default rules.
     */
    public static OperationCtx withSourceSystem(OperationCtx baseCtx, String rawSourceSystem) {
        Objects.requireNonNull(baseCtx, "baseCtx");

        String normalized = DEFAULT_NORMALIZER.normalize(rawSourceSystem);
        Map<String, String> tags = new HashMap<>(baseCtx.tags());
        tags.put(TAG_SOURCE_SYSTEM, normalized);

        return new OperationCtx(baseCtx.metricName(), Map.copyOf(tags));
    }

    // --- Outbound  (generic) ---

    public static <T> Mono<T> timedOutboundMono(ReactiveOperationMetrics metrics,
                                                OperationCtx outboundCtx,
                                                Supplier<Mono<T>> pipelineSupplier) {
        return timedMono(metrics, outboundCtx, pipelineSupplier);
    }

    public static <T> Flux<T> timedOutboundFlux(ReactiveOperationMetrics metrics,
                                                OperationCtx outboundCtx,
                                                Supplier<Flux<T>> pipelineSupplier) {
        return timedFlux(metrics, outboundCtx, pipelineSupplier);
    }
}