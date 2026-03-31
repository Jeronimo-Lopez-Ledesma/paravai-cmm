package com.paravai.communities.resource.infrastructure.persistence.mongo.config;

import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.infrastructure.persistence.mongo.document.ResourceDocument;
import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoFilterConfig {

    @Bean
    public MongoReactiveEntityFilter<Resource, ResourceDocument> serviceFilter(ReactiveMongoTemplate template) {
        return new MongoReactiveEntityFilter<>(
                template,
                ResourceDocument.class,
                ResourceDocument::toDomain,
                "name"
        );
    }

}
