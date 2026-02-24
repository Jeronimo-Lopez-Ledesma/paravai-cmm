package com.paravai.communities.community.infrastructure.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.value.CommunityRulesValue;
import com.paravai.communities.community.domain.value.CommunityStatusValue;
import com.paravai.communities.community.domain.value.CommunityVisibilityValue;
import com.paravai.communities.community.domain.value.ExchangeTypeValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Event payload V1 for Community aggregate.
 *
 * - Used in domain/integration events (EntityChangedEvent and optional specific events)
 * - Provides both "code" and "label" for catalog VOs
 * - Supports snapshot-based reconstruction (for Historization / AuditLog / ReadModels)
 */
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

        CommunityRulesPayloadV1 rules // nullable
) {

    // -------------------------------------------------
    // Domain-based factory
    // -------------------------------------------------

    public static CommunityEventPayloadV1 from(Community c) {
        Objects.requireNonNull(c, "community cannot be null");

        CommunityVisibilityValue visibility = c.visibility();
        CommunityStatusValue status = c.status();

        CommunityRulesPayloadV1 rulesPayload = c.rules()
                .map(CommunityRulesPayloadV1::from)
                .orElse(null);

        return new CommunityEventPayloadV1(
                c.id().toString(),
                c.tenantId().toString(),
                c.name(),
                c.slug(),
                c.description().orElse(null),

                visibility.getCode(),
                visibility.getLabel(),

                status.getCode(),
                status.getLabel(),
                c.archivedAt().orElse(null),

                c.createdAt(),
                c.updatedAt(),
                c.createdBy().toString(),

                rulesPayload
        );
    }

    // -------------------------------------------------
    // Snapshot-based factory
    // -------------------------------------------------

    /**
     * Snapshot-based factory.
     * Reads the JSON produced by SnapshotMapper (aligned with CommunityDocument).
     *
     * Expected snapshot fields (aligned with CommunityDocument):
     * - id, tenantId, name, slug, description
     * - visibilityCode
     * - statusCode, archivedAt
     * - createdBy, createdAt, updatedAt
     * - rulesText, allowedExchangeTypeCodes (optional)
     */
    public static CommunityEventPayloadV1 fromSnapshot(JsonNode json) {
        if (json == null || json.isNull()) {
            throw new IllegalArgumentException("snapshot json cannot be null");
        }

        String id = firstNonNull(text(json, "id"), text(json, "communityId"));
        if (id == null) {
            throw new IllegalArgumentException("snapshot is missing id/communityId");
        }

        String tenantId = text(json, "tenantId");
        if (tenantId == null) {
            throw new IllegalArgumentException("snapshot is missing tenantId");
        }

        String name = text(json, "name");
        String slug = text(json, "slug");
        if (name == null || slug == null) {
            throw new IllegalArgumentException("snapshot is missing name or slug");
        }

        String createdBy = firstNonNull(text(json, "createdBy"), text(json, "createdByUserId"));

        String visibilityCode = firstNonNull(
                text(json, "visibilityCode"),
                text(json, "visibility") // tolerate accidental/legacy naming
        );
        if (visibilityCode == null) {
            throw new IllegalArgumentException("snapshot is missing visibilityCode");
        }

        String statusCode = firstNonNull(
                text(json, "statusCode"),
                text(json, "status") // tolerate accidental/legacy naming
        );
        if (statusCode == null) {
            throw new IllegalArgumentException("snapshot is missing statusCode");
        }

        CommunityVisibilityValue visibility = CommunityVisibilityValue.of(visibilityCode);
        CommunityStatusValue status = CommunityStatusValue.of(statusCode);

        CommunityRulesPayloadV1 rules = readRules(json);

        return new CommunityEventPayloadV1(
                id,
                tenantId,
                name,
                slug,
                text(json, "description"),

                visibility.getCode(),
                visibility.getLabel(),

                status.getCode(),
                status.getLabel(),
                instant(json, "archivedAt"),

                instant(json, "createdAt"),
                instant(json, "updatedAt"),
                createdBy,

                rules
        );
    }

    // -------------------------------------------------
    // Nested payloads
    // -------------------------------------------------

    public record CommunityRulesPayloadV1(
            String text,
            List<ExchangeTypePayloadV1> allowedExchangeTypes
    ) {
        public static CommunityRulesPayloadV1 from(CommunityRulesValue rules) {
            return new CommunityRulesPayloadV1(
                    rules.getText(),
                    rules.getAllowedExchangeTypes().stream()
                            .map(ExchangeTypePayloadV1::from)
                            .toList()
            );
        }
    }

    public record ExchangeTypePayloadV1(
            String code,
            String label
    ) {
        public static ExchangeTypePayloadV1 from(ExchangeTypeValue v) {
            return new ExchangeTypePayloadV1(v.getCode(), v.getLabel());
        }
    }

    // -------------------------------------------------
    // Snapshot parsing helpers
    // -------------------------------------------------

    private static CommunityRulesPayloadV1 readRules(JsonNode json) {
        // Document-aligned fields
        String rulesText = firstNonNull(
                text(json, "rulesText"),
                text(json, "rules") // tolerate legacy naming (string-only rules)
        );

        JsonNode codesArr = json.get("allowedExchangeTypeCodes");
        if (codesArr == null || codesArr.isNull()) {
            // if no codes, treat as no rules (even if rulesText is present)
            // document invariants should avoid this inconsistent state
            return null;
        }
        if (!codesArr.isArray()) {
            throw new IllegalArgumentException("snapshot field allowedExchangeTypeCodes must be an array");
        }

        List<ExchangeTypePayloadV1> allowed = new ArrayList<>();
        for (JsonNode n : codesArr) {
            String code = (n == null || n.isNull()) ? null : n.asText();
            if (code == null || code.isBlank()) continue;

            ExchangeTypeValue v = ExchangeTypeValue.of(code);
            allowed.add(new ExchangeTypePayloadV1(v.getCode(), v.getLabel()));
        }

        if (allowed.isEmpty()) {
            // treat as no rules rather than breaking consumers (but still inconsistent snapshot)
            return null;
        }

        return new CommunityRulesPayloadV1(rulesText, List.copyOf(allowed));
    }

    private static String text(JsonNode node, String field) {
        if (node == null) return null;
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank()) ? null : s;
    }

    private static Instant instant(JsonNode node, String field) {
        String s = text(node, field);
        if (s == null) return null;

        try {
            return Instant.parse(s);
        } catch (Exception ignored) {
            try {
                return Instant.ofEpochMilli(Long.parseLong(s));
            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid instant field '" + field + "': " + s, ex);
            }
        }
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}