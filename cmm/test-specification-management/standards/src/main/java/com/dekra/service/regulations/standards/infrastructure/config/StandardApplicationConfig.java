package com.dekra.service.regulations.standards.infrastructure.config;

import com.dekra.service.regulations.standards.application.common.StandardEventFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StandardApplicationConfig {

    @Bean
    public StandardEventFactory standardEventFactory(@Value("${spring.application.name}") String sourceService) {
        return new StandardEventFactory(sourceService);
    }
}
