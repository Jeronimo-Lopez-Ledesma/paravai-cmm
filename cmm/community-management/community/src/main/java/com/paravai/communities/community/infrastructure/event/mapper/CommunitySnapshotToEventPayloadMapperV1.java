package com.paravai.communities.community.infrastructure.event.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.contracts.event.community.CommunityEventPayloadV1;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class CommunitySnapshotToEventPayloadMapperV1 {

    public CommunityEventPayloadV1 map(JsonNode json) {
        if (json == null || json.isNull()) {
            throw new IllegalArgumentException("Community snapshot must not be null");
        }

        return new CommunityEventPayloadV1(
                requiredText(json, "communityId", "id"),
                requiredText(json, "tenantId"),
                requiredText(json, "name"),
                requiredText(json, "slug"),
                text(json, "description"),
                text(json, "visibilityCode"),
                text(json, "visibilityLabel"),
                text(json, "statusCode"),
                text(json, "statusLabel"),
                instant(json, "archivedAt"),
                instant(json, "createdAt"),
                instant(json, "updatedAt"),
                text(json, "createdBy"),
                mapRules(json.get("rules"))
        );
    }

    private CommunityEventPayloadV1.CommunityRulesPayloadV1 mapRules(JsonNode rulesNode) {
        if (rulesNode == null || rulesNode.isNull()) {
            return null;
        }

        String text = text(rulesNode, "text");
        List<CommunityEventPayloadV1.ExchangeTypePayloadV1> allowed = new ArrayList<>();

        JsonNode allowedNode = rulesNode.get("allowedExchangeTypes");
        if (allowedNode != null && allowedNode.isArray()) {
            for (JsonNode item : allowedNode) {
                allowed.add(new CommunityEventPayloadV1.ExchangeTypePayloadV1(
                        text(item, "code"),
                        text(item, "label")
                ));
            }
        }

        return new CommunityEventPayloadV1.CommunityRulesPayloadV1(text, allowed);
    }

    private String requiredText(JsonNode node, String... fields) {
        String value = firstNonBlank(node, fields);
        if (value == null) {
            throw new IllegalArgumentException("Missing required field. Expected one of: " + String.join(", ", fields));
        }
        return value;
    }

    private String firstNonBlank(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node, field);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return (text == null || text.isBlank()) ? null : text;
    }

    private Instant instant(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            try {
                return Instant.ofEpochMilli(Long.parseLong(value));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid instant field '" + field + "': " + value, ex);
            }
        }
    }
}