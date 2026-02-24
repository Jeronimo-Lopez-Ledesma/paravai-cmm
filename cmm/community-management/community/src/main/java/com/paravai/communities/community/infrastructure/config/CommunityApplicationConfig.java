package com.paravai.communities.community.infrastructure.config;

import com.paravai.communities.community.application.event.CommunityEventFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommunityApplicationConfig {

    @Bean
    public CommunityEventFactory communityEventFactory(@Value("${spring.application.name}") String sourceService) {
        return new CommunityEventFactory(sourceService);
    }
}
