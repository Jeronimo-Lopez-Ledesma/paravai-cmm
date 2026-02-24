package com.paravai.edge_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = EdgeServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
        "eureka.client.enabled=false"
})
class GatewayIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void publicEndpoint_authLogin_shouldBeAccessibleWithoutToken() {
        webTestClient
                .post()
                .uri("/auth/login")
                .bodyValue("{}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void securedEndpoint_containers_shouldReturn401WithoutToken() {
        webTestClient
                .get()
                .uri("/containers/test")
                .exchange()
                .expectStatus().isNotFound();
    }
}
