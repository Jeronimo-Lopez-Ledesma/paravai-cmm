package com.dekra.service.foundation.securityutils.security.apikey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ApiKeyProperties.class)
public class ApiKeyConfig {
    @Bean
    public ApiKeyService apiKeyService(ApiKeyProperties apiKeyProperties) {
        return new ApiKeyService(apiKeyProperties);
    }
}
