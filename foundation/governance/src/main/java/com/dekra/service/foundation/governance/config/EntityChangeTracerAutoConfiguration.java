package com.paravai.foundation.governance.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EntityChangeTracerProperties.class)
public class EntityChangeTracerAutoConfiguration {
}
