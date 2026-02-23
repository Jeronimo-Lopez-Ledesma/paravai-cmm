package com.paravai.foundation.governance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "governance.entity-change-tracer")
public class EntityChangeTracerProperties {
    private String baseUrl;
}

