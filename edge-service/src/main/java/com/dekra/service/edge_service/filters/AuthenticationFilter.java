package com.paravai.edge_service.filters;

import com.paravai.edge_service.metrics.AuthenticationMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final WebClient webClient;
    private final AuthenticationMetricsService metricsService;

    public AuthenticationFilter(ReactiveStringRedisTemplate redisTemplate,
                                AuthenticationMetricsService metricsService,
                                WebClient webClient) {
        this.redisTemplate = redisTemplate;
        this.metricsService = metricsService;
        this.webClient = webClient;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String service = exchange.getRequest().getURI().getPath();
        if (service.contains("/doc-verify")) {
            return chain.filter(exchange);
        }


        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Authentication failed: missing or invalid Authorization header for path {}", service);
            metricsService.incrementAuthFailed();
            return unauthorized(exchange);
        }

        String jwt = authHeader.substring(7);

        return redisTemplate.hasKey(jwt).flatMap(found -> {
            if (Boolean.TRUE.equals(found)) {
                metricsService.incrementAuthSucceededValkey();
                logger.info("Authentication succeeded (Valkey hit) for path {}", service);
                return chain.filter(exchange);

            } else {
                var sample = metricsService.startAzureValidationTimer();
                return webClient.get()
                        .uri("/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .retrieve()
                        .toBodilessEntity()
                        .flatMap(response -> {
                            metricsService.incrementAuthSucceededAzure();
                            metricsService.recordAzureValidationLatency(sample);
                            logger.info("Authentication succeeded (Azure validated) for path {}", service);
                            return redisTemplate.opsForValue()
                                    .set(jwt, "valid", Duration.ofMinutes(15))
                                    .then(chain.filter(exchange));
                        })
                        .onErrorResume(e -> {
                            metricsService.incrementAuthFailedAzure();
                            logger.warn("Authentication failed (Azure validation) for path {}", service);
                            return unauthorized(exchange);
                        });
            }
        })
                .onErrorResume(e -> {
                    metricsService.incrementRedisUnavailable();
                    logger.error("Redis unavailable during authentication for path {}", service, e);
                    return redisUnavailable(exchange);
                });
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> redisUnavailable(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
