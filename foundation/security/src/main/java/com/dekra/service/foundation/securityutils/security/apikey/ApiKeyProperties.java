package com.paravai.foundation.securityutils.security.apikey;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "security")
public class ApiKeyProperties {
    private Map<String, ApiKeyConfig> apiKeys = new HashMap<>();

    @Getter
    @Setter
    public static class ApiKeyConfig {
        private String key;
        private String owner;
        private String oid;
    }
}