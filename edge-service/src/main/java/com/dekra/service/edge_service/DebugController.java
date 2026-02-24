package com.paravai.edge_service;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/debug")
public class DebugController {

    private final ReactiveStringRedisTemplate redisTemplate;

    public DebugController(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/tokens")
    public Mono<List<String>> listTokens() {
        return redisTemplate.keys("*").collectList();
    }

    @GetMapping("/tokens/ttl")
    public Mono<List<TokenWithTTL>> listTokensWithTTL() {
        return redisTemplate.keys("*")
                .flatMap(key ->
                        redisTemplate.getExpire(key)
                                .map(ttl -> new TokenWithTTL(key, ttl.getSeconds()))
                )
                .collectList();
    }

    public record TokenWithTTL(String token, long ttlSeconds) {}

    @DeleteMapping("/tokens/{jwt}")
    public Mono<ResponseEntity<String>> deleteToken(@PathVariable String jwt) {
        return redisTemplate.delete(jwt)
                .map(deleted -> {
                    if (deleted > 0) {
                        return ResponseEntity.ok("Token deleted successfully.");
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

    @DeleteMapping("/tokens")
    public Mono<ResponseEntity<String>> deleteAllTokens() {
        return redisTemplate.keys("*")
                .collectList()
                .flatMap(keys -> {
                    if (keys.isEmpty()) {
                        return Mono.just(ResponseEntity.noContent().build());
                    }
                    return redisTemplate.delete(Flux.fromIterable(keys))
                            .thenReturn(ResponseEntity.ok("All tokens deleted."));
                });
    }





}
