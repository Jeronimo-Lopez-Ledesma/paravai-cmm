package com.paravai.edge_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;


@Configuration
public class FallbackConfiguration {

    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions
                .route(RequestPredicates.GET("/generic-fallback"),
                        this::handleGetFallback)
                .andRoute(RequestPredicates.POST("generic-fallback"),
                        this::handlePostFallback);


    }

    public Mono<ServerResponse> handleGetFallback(ServerRequest request) {
        //return ServerResponse.ok().body(Mono.empty(), String.class);
        return ServerResponse.ok()
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .bodyValue("Fallback response from Edge Service. Please try again later.");
    }

    public Mono<ServerResponse> handlePostFallback(ServerRequest request) {
        return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
