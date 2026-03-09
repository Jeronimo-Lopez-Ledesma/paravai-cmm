package com.paravai.communities.community.infrastructure.event.mapper;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.value.ExchangeTypeValue;
import com.paravai.communities.contracts.event.community.CommunityEventPayloadV1;

public final class CommunityEventPayloadMapperV1 {

    private CommunityEventPayloadMapperV1() {}

    public static CommunityEventPayloadV1 fromDomain(Community c) {
        return new CommunityEventPayloadV1(
                c.id().value(),
                c.tenantId().value(),
                c.name(),
                c.slug(),
                c.description().orElse(null),
                c.visibility().getCode(),
                c.visibility().getLabel(),
                c.status().getCode(),
                c.status().getLabel(),
                c.archivedAt().orElse(null),
                c.createdAt(),
                c.updatedAt(),
                c.createdBy().value(),
                c.rules().map(r -> new CommunityEventPayloadV1.CommunityRulesPayloadV1(
                        r.getText(),
                        r.getAllowedExchangeTypes().stream()
                                .map(CommunityEventPayloadMapper::mapExchangeType)
                                .toList()
                )).orElse(null)
        );
    }

    private static CommunityEventPayloadV1.ExchangeTypePayloadV1 mapExchangeType(ExchangeTypeValue v) {
        return new CommunityEventPayloadV1.ExchangeTypePayloadV1(
                v.getCode(),
                v.getLabel()
        );
    }
}