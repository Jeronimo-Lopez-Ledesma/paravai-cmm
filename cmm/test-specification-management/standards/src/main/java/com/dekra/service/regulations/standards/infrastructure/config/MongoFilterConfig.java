package com.dekra.service.regulations.standards.infrastructure.config;

import com.dekra.service.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.dekra.service.regulations.standards.domain.model.Standard;
import com.dekra.service.regulations.standards.infrastructure.persistence.mongo.document.StandardDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoFilterConfig {

    @Bean
    public MongoReactiveEntityFilter<Standard, StandardDocument> serviceFilter(ReactiveMongoTemplate template) {
        return new MongoReactiveEntityFilter<>(
                template,
                StandardDocument.class,
                StandardDocument::toDomain,
                "name"
        );
    }

}
