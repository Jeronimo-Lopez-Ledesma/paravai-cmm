package com.paravai.edge_service.config;

import com.paravai.edge_service.RedisAuthenticationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisAuthenticationManagerTest {
    @Mock
    ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    ReactiveValueOperations<String, String> valueOps;

    RedisAuthenticationManager manager;

    @BeforeEach
    void setup() {
        manager = new RedisAuthenticationManager(redisTemplate);
    }

    @Test
    void authenticate_shouldReturnAuthentication_whenTokenExistsInRedis() {
        String jwt = "valid-token";
        UsernamePasswordAuthenticationToken input = new UsernamePasswordAuthenticationToken(null, jwt);

        when(redisTemplate.hasKey(jwt)).thenReturn(Mono.just(true));

        StepVerifier.create(manager.authenticate(input))
                .expectNextMatches(auth -> auth.isAuthenticated() && jwt.equals(auth.getCredentials()))
                .verifyComplete();
    }

}