package com.paravai.communities.membership.infrastructure.event.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.contracts.event.membership.MembershipEventPayloadV1;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MembershipSnapshotToEventPayloadMapperV1 {

    public MembershipEventPayloadV1 map(JsonNode json) {
        if (json == null || json.isNull()) {
            throw new IllegalArgumentException("Membership snapshot must not be null");
        }

        return new MembershipEventPayloadV1(
                requiredText(json, "membershipId", "id"),
                requiredText(json, "tenantId"),
                requiredText(json, "communityId"),
                requiredText(json, "userId"),
                requiredText(json, "roleCode"),
                text(json, "roleLabel"),
                requiredText(json, "statusCode"),
                text(json, "statusLabel"),
                instant(json, "since"),
                instant(json, "deactivatedAt"),
                instant(json, "createdAt"),
                instant(json, "updatedAt")
        );
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
        if (node == null) return null;
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) return null;
        String text = value.asText();
        return (text == null || text.isBlank()) ? null : text;
    }

    private Instant instant(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) return null;

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