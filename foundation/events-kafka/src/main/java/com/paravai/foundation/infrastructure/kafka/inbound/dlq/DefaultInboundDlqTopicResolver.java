package com.paravai.foundation.infrastructure.kafka.inbound.dlq;

import com.paravai.foundation.infrastructure.kafka.inbound.config.InboundKafkaProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Objects;

public class DefaultInboundDlqTopicResolver implements InboundDlqTopicResolver {

    private final InboundKafkaProperties properties;

    public DefaultInboundDlqTopicResolver(InboundKafkaProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties is required");
    }

    @Override
    public String resolve(ReceiverRecord<String, byte[]> record) {
        return record.topic() + properties.getDlq().getSuffix();
    }
}