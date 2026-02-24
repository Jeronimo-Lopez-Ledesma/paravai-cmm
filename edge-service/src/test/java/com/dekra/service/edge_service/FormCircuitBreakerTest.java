package com.paravai.edge_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
        "eureka.client.enabled=false"
})
@AutoConfigureWebTestClient
class FormCircuitBreakerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Value("${spring.application.name}")
    private String serviceName;

    private static final String ENDPOINT = "/containers";

    @BeforeEach
    void setUp() {
        assertThat(serviceName).isEqualTo("edge-service");
    }

    @Test
    void shouldTriggerFallbackWhenDownstreamIsUnavailable() {
        for (int i = 0; i < 10; i++) {
            webTestClient.get()
                    .uri(ENDPOINT)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody(String.class)
                    .value(body -> assertThat(body).contains("Fallback"));
        }
    }

    @Test
    void fallbackShouldReturnEmptyListOrMessage() {
        webTestClient.get()
                .uri("/generic-fallback")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).isNotBlank());
    }
}
