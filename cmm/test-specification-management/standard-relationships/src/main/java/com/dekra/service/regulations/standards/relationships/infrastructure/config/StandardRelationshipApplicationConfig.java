package com.dekra.service.regulations.standards.relationships.infrastructure.config;

import com.dekra.service.regulations.standards.relationships.application.common.StandardRelationshipEventFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StandardRelationshipApplicationConfig {

    @Bean
    public StandardRelationshipEventFactory standardEventFactory(@Value("${spring.application.name}") String sourceService) {
        return new StandardRelationshipEventFactory(sourceService);
    }
}
