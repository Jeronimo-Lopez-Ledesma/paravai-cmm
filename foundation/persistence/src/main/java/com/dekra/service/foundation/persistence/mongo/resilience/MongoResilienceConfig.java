package com.dekra.service.foundation.persistence.mongo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MongoResilienceConfig {

    @Bean
    public CircuitBreakerRegistry mongoCircuitBreakerRegistry() {

        CircuitBreakerConfig config =
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(40)                   // % de fallos para abrir
                        .slowCallRateThreshold(50)                 // % de llamadas lentas para abrir
                        .slowCallDurationThreshold(Duration.ofSeconds(2))
                        .waitDurationInOpenState(Duration.ofSeconds(5))
                        .minimumNumberOfCalls(10)
                        .slidingWindowSize(20)
                        .permittedNumberOfCallsInHalfOpenState(5)
                        .build();

        return CircuitBreakerRegistry.of(config);
    }
}
