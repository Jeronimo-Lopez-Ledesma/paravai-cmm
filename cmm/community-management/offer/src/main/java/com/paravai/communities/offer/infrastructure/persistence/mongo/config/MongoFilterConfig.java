package com.paravai.communities.offer.infrastructure.persistence.mongo.config;

import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.infrastructure.persistence.mongo.document.OfferDocument;
import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class MongoFilterConfig {

    @Bean
    public MongoReactiveEntityFilter<Offer, OfferDocument> serviceFilter(ReactiveMongoTemplate template) {
        return new MongoReactiveEntityFilter<>(
                template,
                OfferDocument.class,
                OfferDocument::toDomain,
                "name"
        );
    }

}
