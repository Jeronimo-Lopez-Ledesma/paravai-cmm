package com.paravai.communities.community.infrastructure.event.mapper;

import com.paravai.communities.community.domain.value.CommunityRulesValue;
import com.paravai.communities.contracts.event.community.CommunityEventPayloadV1;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.value.ExchangeTypeValue;
import com.paravai.foundation.domain.value.TimestampValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommunityToEventPayloadMapperV1 {

    public CommunityEventPayloadV1 map(Community community) {
        if (community == null) {
            throw new IllegalArgumentException("Community must not be null");
        }

        return new CommunityEventPayloadV1(
                community.id() != null ? community.id().value() : null,
                community.tenantId() != null ? community.tenantId().value() : null,
                community.name(),
                community.slug(),
                community.description().orElse(null),
                community.visibility() != null ? community.visibility().getCode() : null,
                community.visibility() != null ? community.visibility().getLabel() : null,
                community.status() != null ? community.status().getCode() : null,
                community.status() != null ? community.status().getLabel() : null,
                community.archivedAt().map(TimestampValue::getInstant).orElse(null),
                community.createdAt() != null ? community.createdAt().getInstant() : null,
                community.updatedAt() != null ? community.updatedAt().getInstant() : null,
                community.createdBy() != null ? community.createdBy().toString() : null,
                community.rules().map(this::mapRules).orElse(null)
        );
    }

    private CommunityEventPayloadV1.CommunityRulesPayloadV1 mapRules(CommunityRulesValue rules) {

        if (rules == null) {
            return null;
        }

        return new CommunityEventPayloadV1.CommunityRulesPayloadV1(
                rules.getText(),
                rules.getAllowedExchangeTypes()
                        .stream()
                        .map(this::mapExchangeType)
                        .toList()
        );
    }

    private CommunityEventPayloadV1.ExchangeTypePayloadV1 mapExchangeType(ExchangeTypeValue value) {
        if (value == null) {
            return null;
        }

        return new CommunityEventPayloadV1.ExchangeTypePayloadV1(
                value.getCode(),
                value.getLabel()
        );
    }
}