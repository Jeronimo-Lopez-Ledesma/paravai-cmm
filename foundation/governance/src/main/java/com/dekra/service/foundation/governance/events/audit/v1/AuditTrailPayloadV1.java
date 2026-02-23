package com.dekra.service.foundation.governance.events.audit.v1;

import com.dekra.service.foundation.domaincore.event.EntityChangedEvent;

public record AuditTrailPayloadV1(
        String resourceType,
        String message,   // optional (nullable)
        String userId     // optional (nullable) - if you don’t have it yet, keep null
) {
    public static AuditTrailPayloadV1 from(EntityChangedEvent e) {
        return new AuditTrailPayloadV1(
                e.getResourceType().toString(),
                e.getMessage(),
                null
        );
    }
}