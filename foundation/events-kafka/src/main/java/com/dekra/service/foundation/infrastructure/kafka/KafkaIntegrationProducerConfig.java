package com.dekra.service.foundation.infrastructure.kafka;

import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaIntegrationProducerConfig {

    @Bean
    public KafkaSender<String, DomainEventEnvelope<?>> integrationKafkaSender(
            @Value("${kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${kafka.producer.acks:all}") String acks,
            @Value("${kafka.producer.retries:3}") int retries,
            @Value("${kafka.producer.client-id:${spring.application.name}}") String clientId
    ) {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // producer tuning (optional)
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);

        // Avoid type headers in Spring JsonSerializer
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        SenderOptions<String, DomainEventEnvelope<?>> senderOptions = SenderOptions.create(props);

        return KafkaSender.create(senderOptions);
    }
}

