package com.paravai.foundation.infrastructure.kafka.inbound;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.foundation.integration.application.inbound.InboundEventConsumer;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.infrastructure.kafka.inbound.config.InboundKafkaProperties;
import com.paravai.foundation.infrastructure.kafka.inbound.dlq.InboundDlqPublisher;
import com.paravai.foundation.infrastructure.kafka.inbound.dlq.InboundDlqTopicResolver;

import jakarta.annotation.PostConstruct;
import net.logstash.logback.argument.StructuredArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class KafkaInboundEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaInboundEventConsumer.class);

    private final KafkaReceiver<String, byte[]> receiver;

    private final InboundEventConsumer inboundEventConsumer;
    private final ObjectMapper objectMapper;
    private final InboundDlqPublisher dlqPublisher;
    private final InboundDlqTopicResolver dlqTopicResolver;
    private final InboundKafkaProperties properties;

    public KafkaInboundEventConsumer(
            ReceiverOptions<String, byte[]> baseOptions,
            InboundKafkaProperties properties,
            InboundEventConsumer inboundEventConsumer,
            ObjectMapper objectMapper,
            InboundDlqPublisher dlqPublisher,
            InboundDlqTopicResolver dlqTopicResolver
    ) {

        this.properties = Objects.requireNonNull(properties);
        this.inboundEventConsumer = Objects.requireNonNull(inboundEventConsumer);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.dlqPublisher = Objects.requireNonNull(dlqPublisher);
        this.dlqTopicResolver = Objects.requireNonNull(dlqTopicResolver);

        List<String> topics = properties.topics();

        if (topics.isEmpty()) {
            throw new IllegalStateException("Inbound Kafka bindings require at least one topic");
        }

        log.info(
                "[Inbound][Kafka] Subscribing to topics {}",
                StructuredArguments.value("topics", topics)
        );

        this.receiver = KafkaReceiver.create(
                baseOptions.subscription(topics)
        );
    }

    // -------------------------------------------------------------------------
    // Start consumer
    // -------------------------------------------------------------------------

    @PostConstruct
    public void start() {

        if (!properties.isEnabled()) {
            log.info("[Inbound][Kafka] Consumer disabled");
            return;
        }

        log.info(
                "[Inbound][Kafka] Consumer started",
                StructuredArguments.kv("consumerGroup", properties.getConsumerGroupId())
        );

        receiver.receive()

                .concatMap(record -> processRecord(record))

                .subscribe(
                        null,
                        ex -> log.error(
                                "[Inbound][Kafka] Fatal stream error",
                                StructuredArguments.kv("error", ex.toString()),
                                ex
                        ),
                        () -> log.warn("[Inbound][Kafka] Kafka stream closed unexpectedly")
                );
    }

    // -------------------------------------------------------------------------
    // Record processing
    // -------------------------------------------------------------------------

    private Mono<Void> processRecord(ReceiverRecord <String, byte[]> record) {

        long start = System.nanoTime();
        byte[] rawBytes = record.value();
        String topic = record.topic();

        return handleRecord(rawBytes, topic)

                .retryWhen(
                        Retry.backoff(
                                        properties.getRetry().getMaxAttempts(),
                                        Duration.ofMillis(properties.getRetry().getBackoffMs())
                                )
                                .maxBackoff(Duration.ofMillis(properties.getRetry().getMaxBackoffMs()))
                                .transientErrors(true)

                )

                .then(record.receiverOffset().commit())

                .onErrorResume(ex -> sendToDlq(record, rawBytes, ex))

                .doFinally(signal ->
                        log.debug(
                                "[Inbound][Kafka] Record processed",
                                StructuredArguments.kv("topic", topic),
                                StructuredArguments.kv("durationNs", System.nanoTime() - start)
                        )
                );
    }

    // -------------------------------------------------------------------------
    // Deserialization + dispatch
    // -------------------------------------------------------------------------

    private Mono<Void> handleRecord(byte[] rawBytes, String topic) {

        if (rawBytes == null) {
            log.warn(
                    "[Inbound][Kafka] Null message received",
                    StructuredArguments.kv("topic", topic)
            );
            return Mono.empty();
        }

        DomainEventEnvelope<?> envelope;

        try {

            envelope = objectMapper.readValue(
                    rawBytes,
                    new TypeReference<DomainEventEnvelope<?>>() {}
            );

        } catch (Exception ex) {

            log.error(
                    "[Inbound][Kafka] Deserialization error",
                    StructuredArguments.kv("topic", topic),
                    StructuredArguments.kv("payload", new String(rawBytes, StandardCharsets.UTF_8)),
                    StructuredArguments.kv("exception", ex.toString()),
                    ex
            );

            return Mono.error(ex);
        }

        if (envelope.getEntityType() == null) {

            log.warn(
                    "[Inbound][Kafka] Envelope missing entityType",
                    StructuredArguments.kv("topic", topic)
            );

            return Mono.empty();
        }

        log.debug(
                "[Inbound][Kafka] Event received",
                StructuredArguments.kv("topic", topic),
                StructuredArguments.kv("schemaId", envelope.getSchemaId()),
                StructuredArguments.kv("entityType", envelope.getEntityType()),
                StructuredArguments.kv("entityId", envelope.getEntityId()),
                StructuredArguments.kv("changeType", envelope.getChangeType()),
                StructuredArguments.kv("traceId", envelope.getTraceId())
        );

        return inboundEventConsumer.consume(envelope);
    }

    // -------------------------------------------------------------------------
    // DLQ handling
    // -------------------------------------------------------------------------

    private Mono<Void> sendToDlq(
            ReceiverRecord <String, byte[]> record,
            byte[] rawBytes,
            Throwable ex
    ) {

        if (!properties.getDlq().isEnabled()) {
            return record.receiverOffset().commit();
        }

        String dlqTopic = dlqTopicResolver.resolve(record);

        log.error(
                "[Inbound][Kafka] Event sent to DLQ",
                StructuredArguments.kv("topic", record.topic()),
                StructuredArguments.kv("dlqTopic", dlqTopic),
                StructuredArguments.kv("key", record.key()),
                StructuredArguments.kv("exception", ex.toString()),
                StructuredArguments.kv("payload",
                        rawBytes != null ? new String(rawBytes, StandardCharsets.UTF_8) : null),
                ex
        );

        return dlqPublisher.publishToDlq(
                        dlqTopic,
                        record.key(),
                        rawBytes != null ? new String(rawBytes, StandardCharsets.UTF_8) : null,
                        ex
                )
                .then(record.receiverOffset().commit());
    }
}