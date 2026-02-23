package com.paravai.communities.community.application.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.foundation.domain.value.ResourceTypeValue;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.value.EntityTypeValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.OidValue;
import com.paravai.foundation.domain.value.OperationTypeValue;

import java.util.Objects;

/**
 * Builds EntityChangedEvent for the Standard aggregate with all invariant fields prefilled.
 * Lives in application.common to be reused by all command services.
 */
public final class CommunityEventFactory {

    private final String sourceService;

    public CommunityEventFactory(String sourceService) {
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
                ResourceTypeValue.COMMUNITIES,
                EntityTypeValue.COMMUNITY,
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
