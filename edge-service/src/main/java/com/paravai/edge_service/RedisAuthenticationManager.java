package com.paravai.edge_service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RedisAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisAuthenticationManager(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String jwt = authentication.getCredentials().toString();
        return redisTemplate.hasKey(jwt)
                .flatMap(found -> {
                    if (Boolean.TRUE.equals(found)) {
                        Authentication auth = new UsernamePasswordAuthenticationToken("user", jwt, List.of());
                        return Mono.just(auth);
                    } else {
                        return Mono.empty();
                    }
                });
    }
}
