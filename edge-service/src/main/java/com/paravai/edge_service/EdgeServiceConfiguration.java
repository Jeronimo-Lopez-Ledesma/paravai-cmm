package com.paravai.edge_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class EdgeServiceConfiguration {

    @Bean
    public WebClient graphApiWebClient() {
        return WebClient.builder()
                .baseUrl("https://graph.microsoft.com/v1.0")
                .build();
    }
}
