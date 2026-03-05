package com.paravai.communities.membership.infrastructure.config;

import com.paravai.communities.membership.application.common.MembershipEventFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MembershipApplicationConfig {

    @Bean
    public MembershipEventFactory standardEventFactory(@Value("${spring.application.name}") String sourceService) {
        return new MembershipEventFactory(sourceService);
    }
}
