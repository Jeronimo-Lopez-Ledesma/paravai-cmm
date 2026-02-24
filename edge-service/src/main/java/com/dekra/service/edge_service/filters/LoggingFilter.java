package com.paravai.edge_service.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import reactor.core.publisher.Mono;


import java.net.URI;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst("Trace-Id");
        String path = exchange.getRequest().getPath().toString();
        String method = exchange.getRequest().getMethod().name();

        URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = (route != null) ? route.getId() : "unknown";

        logger.info("Routing request to service [{}] | Method: {} | Path: {} | URI: {} | traceId: {}",
                routeId, method, path, routeUri, traceId);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 2; // Lower priority than TraceId/AuthFilter (-1), but higher than circuit breakers (default ~0+)
    }
}