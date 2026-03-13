package com.paravai.communities.membership.infrastructure.config;

import com.paravai.foundation.infrastructure.kafka.inbound.config.InboundKafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InboundKafkaProperties.class)
public class MembershipInboundKafkaConfig {
}