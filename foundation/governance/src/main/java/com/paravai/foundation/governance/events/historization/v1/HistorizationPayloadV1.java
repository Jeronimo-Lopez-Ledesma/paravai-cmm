package com.paravai.foundation.governance.events.historization.v1;

import com.paravai.foundation.domaincore.event.EntityChangedEvent;
import com.fasterxml.jackson.databind.JsonNode;

public record HistorizationPayloadV1(
        String entityId,
        String entityType,
        boolean fullSnapshot,
        JsonNode payload
) {
    public static HistorizationPayloadV1 from(EntityChangedEvent e) {
        JsonNode snapshot = (e.getCurrentState() != null) ? e.getCurrentState() : e.getPreviousState();
        return new HistorizationPayloadV1(
                e.getEntityId().toString(),
                e.getEntityType().toString(),
                true,
                snapshot
        );
    }
}
