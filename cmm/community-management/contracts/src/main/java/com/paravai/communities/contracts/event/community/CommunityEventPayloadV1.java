package com.paravai.communities.contracts.event.community;

import java.time.Instant;
import java.util.List;

public record CommunityEventPayloadV1(
        String communityId,
        String tenantId,
        String name,
        String slug,
        String description,
        String visibilityCode,
        String visibilityLabel,
        String statusCode,
        String statusLabel,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        CommunityRulesPayloadV1 rules
) {

    public record CommunityRulesPayloadV1(
            String text,
            List<ExchangeTypePayloadV1> allowedExchangeTypes
    ) {}

    public record ExchangeTypePayloadV1(
            String code,
            String label
    ) {}
}