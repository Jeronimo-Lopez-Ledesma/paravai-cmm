package com.dekra.service.foundation.integration.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

public final class DomainEventEnvelope<T> {

    private final String eventId;
    private final String entityId;
    private final String entityType;
    private final String changeType;
    private final String sourceService;
    private final Instant occurredAt;
    private final String traceId;
    private final String userOid;

    /**
     * New canonical schema identifier.
     * Examples:
     *  - tsm.standard.audit.v1
     *  - tsm.standard.historization.v1
     *  - tsm.standard.integration.v1
     */
    private final String schemaId;

    /**
     * Legacy field kept for backward compatibility.
     * Keep it until all consumers are migrated.
     */
    @Deprecated
    private final String version;

    private final T payload;

    @JsonCreator
    public DomainEventEnvelope(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("entityId") String entityId,
            @JsonProperty("entityType") String entityType,
            @JsonProperty("changeType") String changeType,
            @JsonProperty("sourceService") String sourceService,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("traceId") String traceId,
            @JsonProperty("userOid") String userOid,
            @JsonProperty("schemaId") String schemaId,   // NEW
            @JsonProperty("version") String version,     // legacy
            @JsonProperty("payload") T payload
    ) {
        this.eventId = Objects.requireNonNull(eventId);
        this.entityId = Objects.requireNonNull(entityId);
        this.entityType = Objects.requireNonNull(entityType);
        this.changeType = Objects.requireNonNull(changeType);
        this.sourceService = Objects.requireNonNull(sourceService);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.traceId = traceId;
        this.userOid = userOid;

        // Backward compatible fallback:
        // - If schemaId is missing, reuse version (old producers)
        // - If both missing, leave null (allowed)
        this.schemaId = (schemaId != null && !schemaId.isBlank())
                ? schemaId
                : (version != null && !version.isBlank() ? version : null);

        this.version = version; // keep legacy
        this.payload = Objects.requireNonNull(payload);
    }

    public String getEventId() { return eventId; }
    public String getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public String getChangeType() { return changeType; }
    public String getSourceService() { return sourceService; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getTraceId() { return traceId; }
    public String getUserOid() { return userOid; }

    public String getSchemaId() { return schemaId; }

    /** @deprecated use getSchemaId() */
    @Deprecated
    public String getVersion() { return version; }

    public T getPayload() { return payload; }

    public boolean isCreate() { return "CREATED".equalsIgnoreCase(changeType); }
    public boolean isUpdate() { return "UPDATED".equalsIgnoreCase(changeType); }
    public boolean isDelete() { return "DELETED".equalsIgnoreCase(changeType); }
}
