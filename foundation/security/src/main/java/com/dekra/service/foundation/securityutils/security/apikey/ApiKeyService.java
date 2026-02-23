package com.dekra.service.foundation.securityutils.security.apikey;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service("apikeyService")
public class ApiKeyService {
    private final Map<String, ApiKeyProperties.ApiKeyConfig> apiKeys;

    public ApiKeyService(ApiKeyProperties properties) {
        this.apiKeys = properties.getApiKeys();
    }

    public boolean isValid(String apiKey) {
        return apiKeys.values().stream()
                .anyMatch(config -> config.getKey().equals(apiKey));
    }

    public Optional<String> getOwner(String apiKey) {
        return apiKeys.values().stream()
                .filter(config -> config.getKey().equals(apiKey))
                .map(ApiKeyProperties.ApiKeyConfig::getOwner)
                .findFirst();
    }

    public Optional<ApiKeyProperties.ApiKeyConfig> getConfig(String apiKey) {
        return apiKeys.values().stream()
                .filter(config -> config.getKey().equals(apiKey))
                .findFirst();
    }
}
