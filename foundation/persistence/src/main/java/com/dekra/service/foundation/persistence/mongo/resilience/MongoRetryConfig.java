package com.paravai.foundation.persistence.mongo.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class MongoRetryConfig {

    @Bean
    public Retry mongoRetry() {

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1) // retry “imperativo” se deja a 1 → solo registro
                .waitDuration(Duration.ofMillis(0))
                .retryOnResult(result -> false) // nunca reintenta por resultado
                .retryExceptions()              // nunca reintenta excepciones
                .build();

        return Retry.of("mongo-noop-retry", config);
    }
}
