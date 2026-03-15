package com.paravai.edge_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TraceMetricsServiceTest {

    private TraceMetricsService traceMetricsService;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        traceMetricsService = new TraceMetricsService(meterRegistry);
    }

    @Test
    void shouldIncrementDeduplicationCounter() {
        // Act
        traceMetricsService.incrementIgnored();

        // Assert
        Counter counter = meterRegistry.find("gateway_trace_deduplicated_total")
                .tag("result", "ignored")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void shouldAccumulateMultipleIncrements() {
        // Act
        traceMetricsService.incrementIgnored();
        traceMetricsService.incrementIgnored();

        // Assert
        Counter counter = meterRegistry.find("gateway_trace_deduplicated_total")
                .tag("result", "ignored")
                .counter();

        assertThat(counter.count()).isEqualTo(2.0);
    }
}
