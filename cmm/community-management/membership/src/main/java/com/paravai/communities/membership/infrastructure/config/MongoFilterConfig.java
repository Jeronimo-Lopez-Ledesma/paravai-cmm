package com.paravai.communities.membership.infrastructure.config;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.infrastructure.persistence.mongo.document.MembershipDocument;
import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoFilterConfig {

    @Bean
    public MongoReactiveEntityFilter<Membership, MembershipDocument> serviceFilter(ReactiveMongoTemplate template) {
        return new MongoReactiveEntityFilter<>(
                template,
                MembershipDocument.class,
                MembershipDocument::toDomain,
                "name"
        );
    }

}
