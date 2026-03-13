package com.paravai.foundation.infrastructure.kafka.inbound.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.foundation.infrastructure.kafka.inbound.KafkaInboundEventConsumer;
import com.paravai.foundation.infrastructure.kafka.inbound.dlq.DefaultInboundDlqTopicResolver;
import com.paravai.foundation.infrastructure.kafka.inbound.dlq.InboundDlqPublisher;
import com.paravai.foundation.infrastructure.kafka.inbound.dlq.InboundDlqTopicResolver;
import com.paravai.foundation.integration.application.inbound.InboundEventConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.ReceiverOptions;

@Configuration
@ConditionalOnProperty(
        prefix = "integration.inbound.kafka",
        name = "enabled",
        havingValue = "true"
)
@EnableConfigurationProperties(InboundKafkaProperties.class)
public class InboundKafkaAutoConfiguration {

    @Bean
    public InboundDlqTopicResolver inboundDlqTopicResolver(InboundKafkaProperties properties) {
        return new DefaultInboundDlqTopicResolver(properties);
    }

    @Bean
    public KafkaInboundEventConsumer kafkaInboundEventConsumer(
            ReceiverOptions<String, byte[]> baseOptions,
            InboundKafkaProperties properties,
            InboundEventConsumer inboundEventConsumer,
            ObjectMapper objectMapper,
            InboundDlqPublisher dlqPublisher,
            InboundDlqTopicResolver dlqTopicResolver
    ) {
        return new KafkaInboundEventConsumer(
                baseOptions,
                properties,
                inboundEventConsumer,
                objectMapper,
                dlqPublisher,
                dlqTopicResolver
        );
    }
}