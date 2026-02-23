package com.dekra.service.foundation.infrastructure.kafka;

import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import com.dekra.service.foundation.observability.metrics.OperationCtx;
import com.dekra.service.foundation.observability.metrics.ReactiveOperationMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Publishes integration events (DomainEventEnvelope) to Kafka.
 */
@Component
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaIntegrationEventPublisher implements IntegrationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaIntegrationEventPublisher.class);

    // Align with platform buckets already exposed by Actuator
    private static final String METRIC_OUTBOUND_DURATION = "cmm.outbound.operation.duration";

    // Operation tag values
    private static final String OP_PUBLISH_SINGLE = "integration.kafka.publish";
    private static final String OP_PUBLISH_BATCH  = "integration.kafka.publishAll";

    // Counter / summary names (these will show up as independent meters)
    private static final String METRIC_EVENTS_PUBLISHED = "cmm.outbound.events.published";
    private static final String METRIC_EVENTS_FAILED    = "cmm.outbound.events.failed";
    private static final String METRIC_BATCH_SIZE       = "cmm.outbound.events.batch.size";

    private final KafkaSender<String, DomainEventEnvelope<?>> kafkaSender;
    private final KafkaIntegrationTopicResolver topicResolver;

    private final ReactiveOperationMetrics opMetrics;
    private final MeterRegistry registry;

    // Shared instruments
    private final DistributionSummary batchSizeSummary;

    public KafkaIntegrationEventPublisher(
            KafkaSender<String, DomainEventEnvelope<?>> kafkaSender,
            KafkaIntegrationTopicResolver topicResolver,
            ReactiveOperationMetrics opMetrics,
            MeterRegistry registry
    ) {
        this.kafkaSender = Objects.requireNonNull(kafkaSender, "kafkaSender");
        this.topicResolver = Objects.requireNonNull(topicResolver, "topicResolver");
        this.opMetrics = Objects.requireNonNull(opMetrics, "opMetrics");
        this.registry = Objects.requireNonNull(registry, "registry");

        // DistributionSummary should be built once
        this.batchSizeSummary = DistributionSummary.builder(METRIC_BATCH_SIZE)
                .baseUnit("events")
                .description("Batch size for Kafka integration event publishing")
                .register(registry);
    }

    @Override
    public <T> Mono<Void> publish(DomainEventEnvelope<T> envelope) {
        if (envelope == null) {
            return Mono.error(new IllegalArgumentException("envelope cannot be null"));
        }

        final String topic = topicResolver.resolve(envelope);
        final String key = envelope.getEntityId();

        // Safe, low-cardinality dimensions
        final String entityType = safe(envelope.getEntityType());
        final String changeType = safe(envelope.getChangeType());
        final String topicTag = safeTopicTag(topic); // see method below

        OperationCtx ctx = new OperationCtx(
                METRIC_OUTBOUND_DURATION,
                tags(
                        "operation", OP_PUBLISH_SINGLE,
                        "adapter", "kafka",
                        "entityType", entityType,
                        "changeType", changeType,
                        "topic", topicTag
                )
        );

        Mono<Void> sendMono = Mono.defer(() -> {
            ProducerRecord<String, DomainEventEnvelope<?>> record =
                    new ProducerRecord<>(topic, null, key, raw(envelope), buildHeaders(envelope));

            SenderRecord<String, DomainEventEnvelope<?>, String> senderRecord =
                    SenderRecord.create(record, key);

            log.info("Preparing to send integration event {} for entity {} to topic {}",
                    envelope.getChangeType(), envelope.getEntityId(), topic);

            return kafkaSender.send(Mono.just(senderRecord))
                    .doOnNext(result -> {
                        incrementPublished(topicTag, entityType, changeType);

                        log.info("Integration event {} successfully sent to topic {} with offset {}",
                                envelope.getEventId(),
                                topic,
                                result.recordMetadata().offset());
                    })
                    .doOnError(e -> {
                        incrementFailed(topicTag, entityType, changeType, e);

                        log.error("Failed to publish integration event {} to Kafka topic {}",
                                envelope.getEventId(), topic, e);
                    })
                    .then();
        });

        // Timer around the whole outbound send path (including broker ack)
        return opMetrics.timedMono(ctx, sendMono);
    }

    @Override
    public Mono<Void> publishAll(List<? extends DomainEventEnvelope<?>> envelopes) {
        if (envelopes == null || envelopes.isEmpty()) {
            return Mono.empty();
        }

        // Record batch size
        batchSizeSummary.record(envelopes.size());

        // NOTE: in a batch, envelopes may map to different topics/types.
        // We keep the timer tags generic to avoid high cardinality and ambiguity.
        OperationCtx ctx = new OperationCtx(
                METRIC_OUTBOUND_DURATION,
                tags(
                        "operation", OP_PUBLISH_BATCH,
                        "adapter", "kafka"
                )
        );

        Mono<Void> sendMono = Mono.defer(() ->
                kafkaSender.send(
                                Flux.fromIterable(envelopes)
                                        .map(envelope -> {
                                            String topic = topicResolver.resolve(envelope);
                                            String key = envelope.getEntityId();

                                            ProducerRecord<String, DomainEventEnvelope<?>> record =
                                                    new ProducerRecord<>(topic, null, key, raw(envelope), buildHeaders(envelope));

                                            return SenderRecord.create(record, key);
                                        })
                        )
                        .doOnNext(result -> {
                            // We only know topic here; entityType/changeType vary per record.
                            String topicTag = safeTopicTag(result.recordMetadata().topic());
                            incrementPublished(topicTag, "mixed", "mixed");

                            log.info("Integration event batch record sent, topic={}, offset={}",
                                    result.recordMetadata().topic(),
                                    result.recordMetadata().offset());
                        })
                        .doOnError(e -> {
                            incrementFailed("mixed", "mixed", "mixed", e);
                            log.error("Failed to publish integration event batch to Kafka", e);
                        })
                        .then()
        );

        return opMetrics.timedMono(ctx, sendMono);
    }

    // ---------- Counters (bounded tags only) ----------

    private void incrementPublished(String topic, String entityType, String changeType) {
        Counter.builder(METRIC_EVENTS_PUBLISHED)
                .description("Number of integration events successfully published to Kafka")
                .tags(
                        "adapter", "kafka",
                        "topic", topic,
                        "entityType", entityType,
                        "changeType", changeType
                )
                .register(registry)
                .increment();
    }

    private void incrementFailed(String topic, String entityType, String changeType, Throwable error) {
        String errorType = (error != null) ? error.getClass().getSimpleName() : "unknown";

        Counter.builder(METRIC_EVENTS_FAILED)
                .description("Number of integration events that failed to publish to Kafka")
                .tags(
                        "adapter", "kafka",
                        "topic", topic,
                        "entityType", entityType,
                        "changeType", changeType,
                        "errorType", errorType
                )
                .register(registry)
                .increment();
    }

    // ---------- Helpers ----------

    private static RecordHeaders buildHeaders(DomainEventEnvelope<?> envelope) {
        RecordHeaders headers = new RecordHeaders();
        addHeader(headers, "eventId", envelope.getEventId());
        addHeader(headers, "entityId", envelope.getEntityId());
        addHeader(headers, "entityType", envelope.getEntityType());
        addHeader(headers, "changeType", envelope.getChangeType());
        addHeader(headers, "sourceService", envelope.getSourceService());
        addHeader(headers, "traceId", envelope.getTraceId());
        addHeader(headers, "userOid", envelope.getUserOid());
        addHeader(headers, "eventVersion", envelope.getVersion());
        addHeader(headers, "eventType", (envelope.getEntityType() + "." + envelope.getChangeType()));
        return headers;
    }

    private static void addHeader(RecordHeaders headers, String name, String value) {
        if (value != null) {
            headers.add(name, value.getBytes(StandardCharsets.UTF_8));
        }
    }

    @SuppressWarnings("unchecked")
    private static DomainEventEnvelope<?> raw(DomainEventEnvelope<?> envelope) {
        return envelope;
    }

    private static String safe(String v) {
        return (v == null || v.isBlank()) ? "unknown" : v;
    }

    /**
     * Keep topic tag low-cardinality:
     * - If you have a small, stable set of topics, return the topic as-is.
     * - Otherwise, map to a smaller group (e.g., first 2 segments, or a resolver-provided group).
     *
     * Current implementation keeps the topic as-is but normalizes null/blank.
     * If you expect dynamic topic names, replace this with grouping logic.
     */
    private static String safeTopicTag(String topic) {
        return safe(topic);
    }

    private static Map<String, String> tags(String k1, String v1, String k2, String v2) {
        Map<String, String> m = new HashMap<>(4);
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }

    private static Map<String, String> tags(String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4, String k5, String v5) {
        Map<String, String> m = new HashMap<>(10);
        m.put(k1, v1);
        m.put(k2, v2);
        m.put(k3, v3);
        m.put(k4, v4);
        m.put(k5, v5);
        return m;
    }
}
