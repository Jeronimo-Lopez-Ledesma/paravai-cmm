package com.paravai.edge_service;

import com.paravai.edge_service.filters.AuthenticationFilter;
import com.paravai.edge_service.metrics.AuthenticationMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AuthenticationFilterTest {

    private ReactiveStringRedisTemplate redisTemplate;
    private AuthenticationMetricsService metricsService;
    private ReactiveValueOperations<String, String> valueOps;

    @BeforeEach
    public void setUp() {
        redisTemplate = mock(ReactiveStringRedisTemplate.class);
        metricsService = mock(AuthenticationMetricsService.class);
        valueOps = mock(ReactiveValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(anyString(), anyString(), any(Duration.class))).thenReturn(Mono.just(true));
        when(metricsService.startAzureValidationTimer()).thenReturn(Mockito.mock(io.micrometer.core.instrument.Timer.Sample.class));
    }

    @Test
    public void shouldAllowRequestWhenTokenIsCachedInValkey() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(true));

        WebClient dummyWebClient = mock(WebClient.class);
        AuthenticationFilter filter = new AuthenticationFilter(redisTemplate, metricsService, dummyWebClient);

        MockServerHttpRequest request = MockServerHttpRequest.get("/some-secured-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, e -> Mono.empty());
        result.block();

        verify(metricsService).incrementAuthSucceededValkey();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    public void shouldRejectWhenNoAuthorizationHeader() {
        WebClient dummyWebClient = mock(WebClient.class);
        AuthenticationFilter filter = new AuthenticationFilter(redisTemplate, metricsService, dummyWebClient);

        MockServerHttpRequest request = MockServerHttpRequest.get("/some-secured-endpoint").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, e -> Mono.empty());
        result.block();

        verify(metricsService).incrementAuthFailed();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void shouldHandleRedisUnavailable() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.error(new RuntimeException("Redis down")));

        WebClient dummyWebClient = mock(WebClient.class);
        AuthenticationFilter filter = new AuthenticationFilter(redisTemplate, metricsService, dummyWebClient);

        MockServerHttpRequest request = MockServerHttpRequest.get("/some-secured-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, e -> Mono.empty());
        result.block();

        verify(metricsService).incrementRedisUnavailable();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void shouldHandleAzureValidationFailure() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));

        // 1. Mock del ExchangeFunction que WebClient usa para hacer llamadas
        ExchangeFunction mockExchangeFunction = mock(ExchangeFunction.class);

        // 2. Simular que devuelve un error 401 (como lo haría Azure)
        ClientResponse response = ClientResponse
                .create(HttpStatus.UNAUTHORIZED)
                .build();

        when(mockExchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(response));

        // 3. Crear un WebClient real con ese ExchangeFunction simulado
        WebClient webClient = WebClient.builder()
                .exchangeFunction(mockExchangeFunction)
                .baseUrl("https://graph.microsoft.com/v1.0")
                .build();

        // 4. Crear el filtro con este WebClient simulado
        AuthenticationFilter filter = new AuthenticationFilter(redisTemplate, metricsService, webClient);

        MockServerHttpRequest request = MockServerHttpRequest.get("/some-secured-endpoint")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> result = filter.filter(exchange, e -> Mono.empty());
        result.block();

        verify(metricsService).incrementAuthFailedAzure();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }



}
