package com.paravai.foundation.infrastructure.kafka.inbound.dlq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class KafkaInboundDlqPublisher implements InboundDlqPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaInboundDlqPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaInboundDlqPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate is required");
    }

    @Override
    public Mono<Void> publishToDlq(
            String dlqTopic,
            String key,
            String payload,
            Throwable error
    ) {
        return Mono.fromFuture(
                        kafkaTemplate.send(dlqTopic, key, payload != null ? payload : "")
                )
                .doOnSuccess(result -> log.warn(
                        "Inbound event published to DLQ topic={} key={} error={}",
                        dlqTopic,
                        key,
                        error != null ? error.toString() : "n/a"
                ))
                .then();
    }
}
