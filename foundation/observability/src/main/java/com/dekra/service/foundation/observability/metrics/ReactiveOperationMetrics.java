package com.dekra.service.foundation.observability.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ReactiveOperationMetrics {

    private final MeterRegistry registry;

    public ReactiveOperationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public <T> Mono<T> timedMono(OperationCtx ctx, Mono<T> mono) {
        return Mono.defer(() -> {
            Timer.Sample sample = Timer.start(registry);
            AtomicBoolean hasValue = new AtomicBoolean(false);

            return mono
                    .doOnNext(__ -> hasValue.set(true))
                    .doFinally(st -> {
                        String result = resultForMono(st, hasValue.get());
                        stop(sample, ctx, result);
                    });
        });
    }

    public <T> Flux<T> timedFlux(OperationCtx ctx, Flux<T> flux) {
        return Flux.defer(() -> {
            Timer.Sample sample = Timer.start(registry);
            AtomicBoolean hasAny = new AtomicBoolean(false);

            return flux
                    .doOnNext(__ -> hasAny.set(true))
                    .doFinally(st -> {
                        String result = resultForFlux(st, hasAny.get());
                        stop(sample, ctx, result);
                    });
        });
    }

    private void stop(Timer.Sample sample, OperationCtx ctx, String result) {
        Iterable<Tag> tags = toTags(ctx.tags(), result);

        // Create/register the Timer once and stop the sample against that same instance.
        Timer timer = Timer.builder(ctx.metricName())
                .tags(tags)
                .register(registry);

        sample.stop(timer);
    }

    private Iterable<Tag> toTags(Map<String, String> base, String result) {
        List<Tag> tags = new ArrayList<>(base.size() + 1);
        base.forEach((k, v) -> tags.add(Tag.of(k, v)));
        tags.add(Tag.of("result", result));
        return tags;
    }

    private String resultForMono(SignalType st, boolean hasValue) {
        if (st == SignalType.ON_ERROR) return "error";
        if (st == SignalType.CANCEL) return "cancel";
        return hasValue ? "ok" : "empty";
    }

    private String resultForFlux(SignalType st, boolean hasAny) {
        if (st == SignalType.ON_ERROR) return "error";
        if (st == SignalType.CANCEL) return "cancel";
        return hasAny ? "ok" : "empty";
    }
}
