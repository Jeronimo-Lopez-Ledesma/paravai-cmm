package com.paravai.communities.community.infrastructure.config;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.infrastructure.persistence.mongo.document.CommunityDocument;
import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoFilterConfig {

    @Bean
    public MongoReactiveEntityFilter<Community, CommunityDocument> serviceFilter(ReactiveMongoTemplate template) {
        return new MongoReactiveEntityFilter<>(
                template,
                CommunityDocument.class,
                CommunityDocument::toDomain,
                "name"
        );
    }

}
