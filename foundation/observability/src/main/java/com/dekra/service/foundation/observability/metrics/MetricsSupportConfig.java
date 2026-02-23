package com.dekra.service.foundation.observability.metrics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsSupportConfig {

    @Bean
    public SourceSystemTagNormalizer sourceSystemTagNormalizer() {
        return new SourceSystemTagNormalizer();
    }
}