package com.paravai.edge_service;
import com.paravai.edge_service.filters.LoggingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class LoggingFilterTest {

    private LoggingFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new LoggingFilter();
    }

    @Test
    public void shouldLogRoutingInformation() {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/test-path")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummy")
                .header("Trace-Id", "abc-123")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, URI.create("http://localhost/service"));

        Route mockRoute = Mockito.mock(Route.class);
        Mockito.when(mockRoute.getId()).thenReturn("my-service");
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, mockRoute);

        Mono<Void> result = filter.filter(exchange, e -> Mono.empty());
        result.block();

        assertThat(exchange.getRequest().getPath().toString()).isEqualTo("/test-path");
    }
}