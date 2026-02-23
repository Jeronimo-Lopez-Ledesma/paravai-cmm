package com.paravai.regulations.standards.relationships.infrastructure.config;

import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public ReactiveOperationMetrics reactiveOperationMetrics(MeterRegistry registry) {
        return new ReactiveOperationMetrics(registry);
    }
}
