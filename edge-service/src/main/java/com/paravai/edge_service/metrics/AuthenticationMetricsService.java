package com.paravai.edge_service.metrics;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationMetricsService {

    private final Counter totalAuthRequests;
    private final Counter authFailed;
    private final Counter authSucceededValkey;
    private final Counter authSucceededAzure;
    private final Counter authFailedAzure;
    private final Counter redisUnavailable;
    private final MeterRegistry registry;

    public AuthenticationMetricsService(MeterRegistry registry) {
        this.registry = registry;

        this.totalAuthRequests = Counter.builder("gateway_authentication_total")
                .description("Total number of authentication attempts")
                .register(registry);

        this.authFailed = Counter.builder("gateway_authentication_failed")
                .tag("reason", "no_or_invalid_header")
                .description("Authentication failures due to missing or invalid Authorization header")
                .register(registry);

        this.authSucceededValkey = Counter.builder("gateway_authentication_succeeded")
                .tag("source", "valkey")
                .description("Authentication successes using token found in Valkey")
                .register(registry);

        this.authSucceededAzure = Counter.builder("gateway_authentication_succeeded")
                .tag("source", "azure")
                .description("Authentication successes validated through Azure and cached in Valkey")
                .register(registry);

        this.authFailedAzure = Counter.builder("gateway_authentication_failed")
                .tag("reason", "azure_validation")
                .description("Authentication failures due to Azure token validation error")
                .register(registry);

        this.redisUnavailable = Counter.builder("gateway_authentication_failed")
                .tag("reason", "redis_unavailable")
                .description("Authentication failures due to Redis unavailability")
                .register(registry);
    }

    public void incrementTotalRequests() {
        totalAuthRequests.increment();
    }

    public void incrementAuthFailed() {
        authFailed.increment();
    }

    public void incrementAuthSucceededValkey() {
        authSucceededValkey.increment();
    }

    public void incrementAuthSucceededAzure() {
        authSucceededAzure.increment();
    }

    public void incrementAuthFailedAzure() {
        authFailedAzure.increment();
    }

    public void incrementRedisUnavailable() {
        redisUnavailable.increment();
    }

    public Timer.Sample startAzureValidationTimer() {
        return Timer.start(registry);
    }

    public void recordAzureValidationLatency(Timer.Sample sample) {
        sample.stop(Timer.builder("gateway_authentication_latency")
                .description("Latency of Azure token validation")
                .publishPercentileHistogram()
                .register(registry));
    }
}
