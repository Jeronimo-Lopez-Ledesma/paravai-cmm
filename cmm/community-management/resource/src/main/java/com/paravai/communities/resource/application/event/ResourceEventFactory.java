package com.paravai.communities.resource.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.value.EntityTypeValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.OidValue;
import com.paravai.foundation.domain.value.OperationTypeValue;
import com.paravai.foundation.domain.value.ResourceTypeValue;

import java.util.Objects;

/**
 * Builds EntityChangedEvent for the aggregate with all invariant fields prefilled.
 * Lives in application.event to be reused by all command services.
 */
public final class ResourceEventFactory {

    private final String sourceService;

    public ResourceEventFactory(String sourceService) {
        this.sourceService = Objects.requireNonNull(sourceService, "sourceService");
    }

    public EntityChangedEvent build(OperationTypeValue op,
                                    IdValue entityId,
                                    String traceId,
                                    String userOid,
                                    String sourceSystem,
                                    String message,
                                    JsonNode prev,
                                    JsonNode current) {

        return new EntityChangedEvent(
                entityId,
                ResourceTypeValue.RESOURCES,
                EntityTypeValue.RESOURCE,
                OidValue.of(userOid),
                IdValue.of(traceId),
                sourceSystem,
                op,
                message,
                prev,
                current
        );
    }
}