package com.paravai.edge_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TraceMetricsService {
    private final Counter deduplicationIgnored;

    public TraceMetricsService(MeterRegistry registry) {
        this.deduplicationIgnored = Counter.builder("gateway_trace_deduplicated_total")
                .tag("result", "ignored")
                .description("Total number of traceIds ignored due to deduplication")
                .register(registry);
    }

    public void incrementIgnored() {
        deduplicationIgnored.increment();
    }
}
