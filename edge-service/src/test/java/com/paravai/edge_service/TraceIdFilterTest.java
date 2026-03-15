package com.paravai.edge_service;

import com.paravai.edge_service.filters.TraceIdFilter;
import com.paravai.edge_service.metrics.TraceMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class TraceIdFilterTest {

    private ReactiveStringRedisTemplate redisTemplate;
    private TraceMetricsService traceMetricsService;
    private TraceIdFilter filter;

    @BeforeEach
    public void setUp() {
        redisTemplate = Mockito.mock(ReactiveStringRedisTemplate.class);
        traceMetricsService = Mockito.mock(TraceMetricsService.class);

        // Nuevo mock para opsForValue
        ReactiveValueOperations<String, String> valueOps = Mockito.mock(ReactiveValueOperations.class);
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // Guardar para usar en los tests
        this.filter = new TraceIdFilter(redisTemplate, traceMetricsService);

        // Por defecto, devolver true (puedes sobrescribir esto en cada test si lo necesitas)
        Mockito.when(valueOps.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(Mono.just(true));
    }


    @Test
    public void shouldAllowRequestWhenTraceIdIsNew() {
        Mockito.when(redisTemplate.opsForValue().setIfAbsent(anyString(), any(), any()))
                .thenReturn(Mono.just(true));

        MockServerHttpRequest request = MockServerHttpRequest.get("/some-path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, e -> Mono.empty()))
                .verifyComplete();

        ServerHttpResponse response = exchange.getResponse();
        assertThat(response.getStatusCode()).isNull(); // no error was set
    }

    @Test
    public void shouldRejectRequestWhenTraceIdAlreadyExists() {

        Mockito.when(redisTemplate.opsForValue().setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(Mono.just(false));


        MockServerHttpRequest request = MockServerHttpRequest.get("/some-path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, e -> Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Mockito.verify(traceMetricsService).incrementIgnored();
    }

    @Test
    public void shouldGenerateTraceIdIfNotProvided() {
        Mockito.when(redisTemplate.opsForValue().setIfAbsent(anyString(), any(), any()))
                .thenReturn(Mono.just(true));

        MockServerHttpRequest request = MockServerHttpRequest.get("/some-path").build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, e -> Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }
}
