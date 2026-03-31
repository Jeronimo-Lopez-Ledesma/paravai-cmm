package com.paravai.communities.offer.application.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.domain.value.*;

import java.util.Objects;

/**
 * Builds EntityChangedEvent for Offer aggregate with all invariant fields prefilled.
 */
public final class OfferEventFactory {

    private final String sourceService;

    public OfferEventFactory(String sourceService) {
        this.sourceService = Objects.requireNonNull(sourceService, "sourceService");
    }

    public EntityChangedEvent build(
            OperationTypeValue op,
            IdValue entityId,
            String traceId,
            String userOid,
            String sourceSystem,
            String message,
            JsonNode prev,
            JsonNode current
    ) {
        return new EntityChangedEvent(
                entityId,
                ResourceTypeValue.OFFERS,
                EntityTypeValue.OFFER,
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