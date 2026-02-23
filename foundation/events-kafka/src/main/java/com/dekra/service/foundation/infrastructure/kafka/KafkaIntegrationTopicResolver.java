package com.dekra.service.foundation.infrastructure.kafka;

import com.dekra.service.foundation.integration.domain.event.DomainEventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Kafka topic resolver (platform standard).
 *
 * Canonical strategy:
 *   - topic = <topicPrefix>.<schemaId>
 *   - schemaId example: tspecm.standard.integration.v1
 *
 * Legacy fallback (only if schemaId missing):
 *   - entityType + legacyPattern + legacyVersion
 *
 * Priority (highest -> lowest):
 *   1) schemaId override         schemaTopics[schemaId]
 *   2) schemaId as topic         <prefix>.<schemaId>
 *   3) entityType override       entityTopics[ENTITYTYPE]
 *   4) legacyPattern fallback    <prefix>.<entityType.events.v1>
 *   5) defaultTopic
 */
@Component
public class KafkaIntegrationTopicResolver {

    private static final Logger log = LoggerFactory.getLogger(KafkaIntegrationTopicResolver.class);

    // Preferred overrides: schemaId -> topic
    private final Map<String, String> schemaTopics = new HashMap<>();

    // Legacy overrides: ENTITYTYPE -> topic
    private final Map<String, String> entityTopics = new HashMap<>();

    private final String topicPrefix;
    private final String defaultTopic;
    private static final String AUDIT_SEGMENT = ".audit.";
    private static final String HISTORIZATION_SEGMENT = ".historization.";
    private final String historizationTopic;
    private final String auditTopic;

    /**
     * Legacy pattern (used only when schemaId is missing).
     * Default: "%s.events.%s" -> "opportunity.events.v1"
     */
    private final String legacyPattern;

    public KafkaIntegrationTopicResolver(
            @Value("${kafka.integration.topic-prefix:}") String topicPrefix,
            @Value("${kafka.integration.default-topic:domain-events}") String defaultTopic,
            @Value("${kafka.integration.legacy-pattern:%s.events.%s}") String legacyPattern,
            @Value("${kafka.integration.audit-topic:governance.auditlog.events.v1}") String auditTopic,
            @Value("${kafka.integration.historization-topic:governance.historization.events.v1}") String historizationTopic
    ) {
        this.topicPrefix = normalizePrefix(topicPrefix);
        this.defaultTopic = Objects.requireNonNull(defaultTopic, "defaultTopic");
        this.legacyPattern = legacyPattern;
        this.auditTopic = Objects.requireNonNull(auditTopic, "auditTopic");
        this.historizationTopic = Objects.requireNonNull(historizationTopic, "historizationTopic");

    }

    public String resolve(DomainEventEnvelope<?> envelope) {
        if (envelope == null) return defaultTopic;

        // 1) schemaId override / canonical schemaId topic
        String schemaId = normalizeSchemaId(envelope.getSchemaId());
        if (schemaId != null) {
            if (schemaId.contains(AUDIT_SEGMENT)) {
                return withPrefix(auditTopic);
            }
            if (schemaId.contains(HISTORIZATION_SEGMENT)) {
                return withPrefix(historizationTopic);
            }

            String override = schemaTopics.get(schemaId);
            if (override != null && !override.isBlank()) {
                log.debug("[TopicResolver] schemaId override matched schemaId={} -> {}", schemaId, override);
                return withPrefix(override);
            }
            // 2) schemaId -> topic (canonical)
            return withPrefix(schemaId);
        }

        // ----- LEGACY fallback -----
        String entityType = envelope.getEntityType();
        if (entityType == null || entityType.isBlank()) {
            log.warn("[TopicResolver] schemaId missing and entityType missing. Using defaultTopic={}", defaultTopic);
            return defaultTopic;
        }

        String upperType = normalizeEntityTypeKey(entityType);
        String lowerType = entityType.toLowerCase(Locale.ROOT);

        // 3) entityType override
        String entityOverride = entityTopics.get(upperType);
        if (entityOverride != null && !entityOverride.isBlank()) {
            log.debug("[TopicResolver] entityType override matched entityType={} -> {}", upperType, entityOverride);
            return withPrefix(entityOverride);
        }

        // 4) legacy pattern fallback
        String version = envelope.getVersion(); // legacy
        if (version == null || version.isBlank()) version = "v1";

        if (legacyPattern != null && !legacyPattern.isBlank()) {
            String topic = String.format(legacyPattern, lowerType, version.toLowerCase(Locale.ROOT));
            return withPrefix(topic);
        }

        // 5) default fallback
        return defaultTopic;
    }

    // For consumers: prefer schemaId
    public String resolveForSchemaId(String schemaId) {
        String normalized = normalizeSchemaId(schemaId);
        if (normalized == null) return defaultTopic;

        String override = schemaTopics.get(normalized);
        if (override != null && !override.isBlank()) return withPrefix(override);

        return withPrefix(normalized);
    }

    // For old consumers: legacy fallback helper
    public String resolveForEntityType(String entityType, String legacyVersion) {
        if (entityType == null || entityType.isBlank()) return defaultTopic;

        String upperType = normalizeEntityTypeKey(entityType);
        String lowerType = entityType.toLowerCase(Locale.ROOT);

        String entityOverride = entityTopics.get(upperType);
        if (entityOverride != null && !entityOverride.isBlank()) return withPrefix(entityOverride);

        if (legacyVersion == null || legacyVersion.isBlank()) legacyVersion = "v1";

        if (legacyPattern != null && !legacyPattern.isBlank()) {
            return withPrefix(String.format(legacyPattern, lowerType, legacyVersion.toLowerCase(Locale.ROOT)));
        }

        return defaultTopic;
    }

    // ---- Configuration / overrides -----------------------------------------

    public Map<String, String> getSchemaTopics() {
        return schemaTopics;
    }

    /**
     * Overrides by schemaId.
     *
     * Input keys are normalized:
     *   - trim
     *   - lowercase
     *
     * So both "TSPECM.Standard.Integration.V1" and "tspecm.standard.integration.v1" map to same entry.
     */
    public void setSchemaTopics(Map<String, String> overrides) {
        if (overrides == null) return;

        overrides.forEach((k, v) -> {
            String key = normalizeSchemaId(k);
            String value = normalizeTopicValue(v);
            if (key != null && value != null) {
                schemaTopics.put(key, value);
            }
        });
    }

    public Map<String, String> getEntityTopics() {
        return entityTopics;
    }

    /**
     * Legacy overrides by entityType.
     *
     * Input keys are normalized:
     *   - trim
     *   - uppercase
     */
    public void setEntityTopics(Map<String, String> overrides) {
        if (overrides == null) return;

        overrides.forEach((k, v) -> {
            String key = normalizeEntityTypeKey(k);
            String value = normalizeTopicValue(v);
            if (key != null && value != null) {
                entityTopics.put(key, value);
            }
        });
    }

    // ---- helpers ------------------------------------------------------------

    /**
     * Applies prefix unless:
     *  - prefix is blank, or
     *  - topic already starts with "<prefix>."
     *
     * This prevents "double prefix" when overrides already include it.
     */
    private String withPrefix(String topic) {
        if (topic == null || topic.isBlank()) return defaultTopic;

        // GLOBAL TOPICS: do not apply CMM prefix
        if (topic.startsWith("governance.")) {
            return topic;
        }

        if (topicPrefix == null || topicPrefix.isBlank()) return topic;

        String p = topicPrefix + ".";
        if (topic.startsWith(p)) return topic;

        return p + topic;
    }

    private static String normalizeSchemaId(String schemaId) {
        if (schemaId == null || schemaId.isBlank()) return null;
        return schemaId.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeEntityTypeKey(String entityType) {
        if (entityType == null || entityType.isBlank()) return null;
        return entityType.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizePrefix(String prefix) {
        if (prefix == null) return null;
        String p = prefix.trim();
        while (p.endsWith(".")) {
            p = p.substring(0, p.length() - 1);
        }
        return p.isBlank() ? null : p;
    }

    private static String normalizeTopicValue(String topic) {
        if (topic == null) return null;
        String t = topic.trim();
        while (t.endsWith(".")) {
            t = t.substring(0, t.length() - 1);
        }
        return t.isBlank() ? null : t;
    }
}
