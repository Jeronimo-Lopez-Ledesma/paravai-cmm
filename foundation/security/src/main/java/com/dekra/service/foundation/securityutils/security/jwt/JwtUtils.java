package com.dekra.service.foundation.securityutils.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtUtils {

    private static final WebClient graphClient = WebClient
            .builder()
            .baseUrl("https://graph.microsoft.com/v1.0")
            .build();

    public static Mono<String> getToken (ServerWebExchange exchange){
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized("Missing or invalid Authorization header");
        }
        String jwt = authHeader.substring(7);

        return Mono.just(jwt);
    }

    public static Mono<String> getOidFromJwtSimple(String jwt) {
        try {
            DecodedJWT decoded = JWT.decode(jwt);
            String oid = decoded.getClaim("oid").asString();
            if (oid == null) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "JWT without claim 'oid'"));
            }
            return Mono.just(oid);
        } catch (Exception e) {
            return unauthorized("Error decoding JWT");
        }
    }

    public static Mono<JsonNode> getMe(String jwt) {
        return graphClient.get()
                .uri("/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorMap(e ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Error fetching /me from Graph API", e
                        )
                );
    }

    private static Mono<String> unauthorized(String message) {
        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, message));
    }
}
