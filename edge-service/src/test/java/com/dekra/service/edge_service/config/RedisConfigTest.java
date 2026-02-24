package com.paravai.edge_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "eureka.client.enabled=false"
})
class RedisConfigTest {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Test
    void reactiveStringRedisTemplate_shouldBeLoaded() {
        assertThat(redisTemplate).isNotNull();
    }
}