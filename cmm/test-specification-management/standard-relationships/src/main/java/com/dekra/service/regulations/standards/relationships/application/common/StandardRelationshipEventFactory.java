package com.dekra.service.regulations.standards.relationships.application.common;

import com.dekra.service.foundation.domain.value.ResourceTypeValue;
import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;
import com.dekra.service.foundation.domaincore.value.EntityTypeValue;
import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.OidValue;
import com.dekra.service.foundation.domaincore.value.OperationTypeValue;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

/**
 * Builds EntityChangedEvent for the Standard Relationship aggregate with all invariant fields prefilled.
 * Lives in application.common to be reused by all command services.
 */
public final class StandardRelationshipEventFactory {

    private final String sourceService;

    public StandardRelationshipEventFactory(String sourceService) {
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
                ResourceTypeValue.STANDARD_RELATIONSHIPS,
                EntityTypeValue.STANDARD_RELATIONSHIP,
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
