package com.paravai.edge_service.filters;

import com.paravai.edge_service.metrics.TraceMetricsService;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Component
public class TraceIdFilter implements GlobalFilter, Ordered {
    public static final String TRADEIDHEADER = "Trace-Id";
    private final ReactiveStringRedisTemplate redisTemplate;
    private final TraceMetricsService traceMetricsService;

    public TraceIdFilter(ReactiveStringRedisTemplate redisTemplate, TraceMetricsService traceMetricsService) {
        this.redisTemplate = redisTemplate;
        this.traceMetricsService = traceMetricsService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = checkTraceId(exchange);

        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header(TRADEIDHEADER, traceId)
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return Mono.deferContextual(ctx -> {
            MDC.put("traceId", traceId);
            return redisTemplate.opsForValue()
                    .setIfAbsent(traceId, "valid", Duration.ofMinutes(30))
                    .flatMap(wasSet -> {
                        if (Boolean.TRUE.equals(wasSet)) {
                            return chain.filter(mutatedExchange)
                                    .doFinally(signal -> MDC.clear());
                        } else {
                            traceMetricsService.incrementIgnored();
                            byte[] bytes = "La clave ya existe en Valkey".getBytes(StandardCharsets.UTF_8);
                            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
                            exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
                            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                                            .bufferFactory().wrap(bytes)))
                                    .doFinally(signal -> MDC.clear());
                        }
                    });
        }).contextWrite(Context.of("traceId", traceId));
    }

    private String checkTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRADEIDHEADER);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
