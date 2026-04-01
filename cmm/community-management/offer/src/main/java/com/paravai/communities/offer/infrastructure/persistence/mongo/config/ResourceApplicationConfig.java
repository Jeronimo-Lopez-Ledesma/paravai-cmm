package com.paravai.communities.offer.infrastructure.persistence.mongo.config;

import com.paravai.communities.offer.application.event.OfferEventFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceApplicationConfig {

    @Bean
    public OfferEventFactory resourceEventFactory(@Value("${spring.application.name}") String sourceService) {
        return new OfferEventFactory(sourceService);
    }
}
