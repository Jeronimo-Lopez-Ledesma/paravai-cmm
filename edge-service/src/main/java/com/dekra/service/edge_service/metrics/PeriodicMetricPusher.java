package com.paravai.edge_service.metrics;


import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PeriodicMetricPusher {

    private final MeterRegistry meterRegistry;

    public PeriodicMetricPusher(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Registra la métrica en el arranque
        meterRegistry.counter("startup_push_counter").increment();
    }

    @Scheduled(fixedRate = 10000) // cada 10 segundos
    public void pushMetric() {
        meterRegistry.counter("periodic_push_counter").increment();
    }
}