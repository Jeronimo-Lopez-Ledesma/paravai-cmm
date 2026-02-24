package com.paravai.edge_service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StartupMetricsVerifier {

    private static final Logger logger = LoggerFactory.getLogger(StartupMetricsVerifier.class);
    private final MeterRegistry meterRegistry;

    public StartupMetricsVerifier(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void verifyMetrics() {
        logger.info("StartupMetricsVerifier initialized: MeterRegistry is active");
        meterRegistry.counter("startup_test_counter", "env", "dev").increment();
        logger.info("startup_test_counter metric incremented");
    }
}
