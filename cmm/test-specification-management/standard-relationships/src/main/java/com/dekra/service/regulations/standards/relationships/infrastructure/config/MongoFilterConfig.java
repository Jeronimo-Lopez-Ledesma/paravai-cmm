package com.paravai.regulations.standards.relationships.infrastructure.config;

import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.paravai.regulations.standards.relationships.domain.model.StandardRelationship;
import com.paravai.regulations.standards.relationships.infrastructure.persistence.mongo.document.StandardRelationshipDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoFilterConfig {

    @Bean
    public MongoReactiveEntityFilter<StandardRelationship, StandardRelationshipDocument> serviceFilter(ReactiveMongoTemplate template) {
        return new MongoReactiveEntityFilter<>(
                template,
                StandardRelationshipDocument.class,
                StandardRelationshipDocument::toDomain,
                "name"
        );
    }

}
