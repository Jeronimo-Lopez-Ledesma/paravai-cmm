package com.dekra.service.foundation.persistence.mongo.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class MongoCircuitBreakerRegistryFactory {

    private static final Logger log =
            LoggerFactory.getLogger(MongoCircuitBreakerRegistryFactory.class);

    private final CircuitBreakerRegistry registry;

    public MongoCircuitBreakerRegistryFactory() {

        CircuitBreakerConfig defaultConfig =
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(40)                   // open si 40% fallos
                        .slowCallRateThreshold(50)                 // slow calls > 50%
                        .slowCallDurationThreshold(Duration.ofSeconds(2))
                        .waitDurationInOpenState(Duration.ofSeconds(5))
                        .minimumNumberOfCalls(10)
                        .slidingWindowSize(20)
                        .permittedNumberOfCallsInHalfOpenState(5)
                        .build();

        this.registry = CircuitBreakerRegistry.of(defaultConfig);

        log.info("[MongoCB] Default CircuitBreakerRegistry initialized");
    }

    /**
     * Creates or retrieves a CircuitBreaker following a strict naming convention:
     *
     * mongo.{repository}.{operation}
     *
     * Examples:
     * mongo.opportunity-read.findById
     * mongo.organization-read.save
     * mongo.readmodel.count
     */
    public CircuitBreaker getBreaker(String repository, String operation) {

        String name = String.format("mongo.%s.%s",
                repository.toLowerCase(),
                operation.toLowerCase()
        );

        CircuitBreaker cb = registry.circuitBreaker(name);

        log.debug("[MongoCB] CircuitBreaker retrieved: {}", name);

        return cb;
    }

    public CircuitBreakerRegistry registry() {
        return this.registry;
    }
}
