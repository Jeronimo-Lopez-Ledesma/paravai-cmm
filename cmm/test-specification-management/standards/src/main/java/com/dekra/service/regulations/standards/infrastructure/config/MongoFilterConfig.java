package com.paravai.regulations.standards.infrastructure.config;

import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.infrastructure.persistence.mongo.document.StandardDocument;
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
