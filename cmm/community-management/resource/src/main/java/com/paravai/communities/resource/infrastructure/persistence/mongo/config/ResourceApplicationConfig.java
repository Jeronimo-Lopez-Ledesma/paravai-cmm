package com.paravai.communities.resource.infrastructure.persistence.mongo.config;

import com.paravai.communities.resource.application.event.ResourceEventFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceApplicationConfig {

    @Bean
    public ResourceEventFactory resourceEventFactory(@Value("${spring.application.name}") String sourceService) {
        return new ResourceEventFactory(sourceService);
    }
}
