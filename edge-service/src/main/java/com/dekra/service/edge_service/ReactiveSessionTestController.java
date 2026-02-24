package com.paravai.edge_service;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/session-test")
public class ReactiveSessionTestController {

    @PostMapping("/set")
    public Mono<String> setAttribute(ServerWebExchange exchange) {
        return exchange.getSession()
                .doOnNext(webSession -> webSession.getAttributes().put("user", "jero"))
                .map(webSession -> "Session set with ID: " + webSession.getId());
    }

    @GetMapping("/get")
    public Mono<String> getAttribute(ServerWebExchange exchange) {
        return exchange.getSession()
                .map(webSession -> {
                    Object user = webSession.getAttribute("user");
                    return user != null
                            ? "Session contains user: " + user + " (ID: " + webSession.getId() + ")"
                            : "No attribute 'user' in session (ID: " + webSession.getId() + ")";
                });
    }
}