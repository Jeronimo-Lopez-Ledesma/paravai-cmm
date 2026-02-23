package com.dekra.service.foundation.domaincore.event;

import com.dekra.service.foundation.domain.value.ResourceTypeValue;
import com.dekra.service.foundation.domaincore.value.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityChangedEvent extends DomainEvent {

    private final IdValue entityId;
    private final ResourceTypeValue resourceType;
    private final EntityTypeValue entityType;

    private final OidValue userOid;
    private final IdValue traceId;

    /**
     * Canonical field: system/component that originated the request (caller).
     * Examples: "portal-ui", "one", "medical", "batch-import", "sparta-bff"
     */
    @JsonProperty("sourceSystem")
    @JsonAlias({"sourceService"}) // legacy JSON name support
    private final String sourceSystem;

    private final OperationTypeValue operationType;
    private final String message;

    private final JsonNode previousState;
    private final JsonNode currentState;

    /**
     * Optional structured change metadata.
     * Backward-compatible: may be null for older producers.
     */
    private final List<AggregateChangeDescriptorValue> changes;

    // ----------------------------------------------------------------------
    // Backward-compatible constructor (kept): parameter name was sourceService
    // Semantics now: sourceService == sourceSystem
    // ----------------------------------------------------------------------
    public EntityChangedEvent(
            IdValue entityId,
            ResourceTypeValue resourceType,
            EntityTypeValue entityType,
            OidValue userOid,
            IdValue traceId,
            String sourceService,               // legacy param name
            OperationTypeValue operationType,
            String message,
            JsonNode previousState,
            JsonNode currentState
    ) {
        this(entityId, resourceType, entityType, userOid, traceId, sourceService,
                operationType, message, previousState, currentState, null);
    }

    // ----------------------------------------------------------------------
    // Canonical constructor
    // ----------------------------------------------------------------------
    public EntityChangedEvent(
            IdValue entityId,
            ResourceTypeValue resourceType,
            EntityTypeValue entityType,
            OidValue userOid,
            IdValue traceId,
            String sourceSystem,
            OperationTypeValue operationType,
            String message,
            JsonNode previousState,
            JsonNode currentState,
            List<AggregateChangeDescriptorValue> changes
    ) {
        super();

        this.entityId = Objects.requireNonNull(entityId, "entityId");
        this.resourceType = Objects.requireNonNull(resourceType, "resourceType");
        this.entityType = Objects.requireNonNull(entityType, "entityType");

        this.userOid = Objects.requireNonNull(userOid, "userOid");
        this.traceId = Objects.requireNonNull(traceId, "traceId");

        // canonical
        this.sourceSystem = Objects.requireNonNull(sourceSystem, "sourceSystem");

        this.operationType = Objects.requireNonNull(operationType, "operationType");
        this.message = message;

        this.previousState = previousState;
        this.currentState = currentState;

        this.changes = changes;
    }

    /**
     * Safe accessor to avoid null checks.
     */
    public List<AggregateChangeDescriptorValue> getChangesOrEmpty() {
        return changes == null ? List.of() : changes;
    }

    /**
     * Legacy getter kept for source compatibility with existing code.
     * @deprecated use getSourceSystem()
     */
    @Deprecated
    public String getSourceService() {
        return sourceSystem;
    }
}
