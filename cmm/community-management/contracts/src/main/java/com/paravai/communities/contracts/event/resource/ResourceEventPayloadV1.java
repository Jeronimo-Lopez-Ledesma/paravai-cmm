package com.paravai.communities.contracts.event.resource;

import java.time.Instant;

public record ResourceEventPayloadV1(
        String resourceId,
        String tenantId,
        String ownerId,

        String title,
        String description,

        String conditionCode,

        Instant createdAt,
        Instant updatedAt
) {}