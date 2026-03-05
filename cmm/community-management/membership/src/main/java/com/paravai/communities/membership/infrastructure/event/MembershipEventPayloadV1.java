package com.paravai.communities.membership.infrastructure.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.value.CommunityRoleValue;
import com.paravai.communities.membership.domain.value.MembershipStatusValue;

import java.time.Instant;

public record MembershipEventPayloadV1(
        String membershipId,
        String tenantId,
        String communityId,
        String userId,

        String roleCode,
        String roleLabel,

        String statusCode,
        String statusLabel,

        Instant since,
        Instant deactivatedAt,

        Instant createdAt,
        Instant updatedAt
) {

    public static MembershipEventPayloadV1 from(Membership m) {
        if (m == null) throw new IllegalArgumentException("membership cannot be null");

        return new MembershipEventPayloadV1(
                m.id().toString(),
                m.tenantId().toString(),
                m.communityId().toString(),
                m.userId().toString(),

                m.role().getCode(),
                m.role().getLabel(),

                m.status().getCode(),
                m.status().getLabel(),

                m.since(),
                m.deactivatedAt(),

                m.createdAt(),
                m.updatedAt()
        );
    }

    /**
     * Snapshot-based factory.
     * Reads the JSON produced by SnapshotMapper (aligned with MembershipDocument).
     */
    public static MembershipEventPayloadV1 fromSnapshot(JsonNode json) {
        if (json == null || json.isNull()) {
            throw new IllegalArgumentException("snapshot json cannot be null");
        }

        // tolerate different field names if something changed in documents over time
        String membershipId = firstNonNull(text(json, "id"), text(json, "membershipId"));
        String tenantId = firstNonNull(text(json, "tenantId"), text(json, "tenantRefId"));
        String communityId = firstNonNull(text(json, "communityId"), text(json, "communityRefId"));
        String userId = firstNonNull(text(json, "userId"), text(json, "memberUserId"), text(json, "inviteeUserId"));

        String roleCode = firstNonNull(text(json, "roleCode"), text(json, "role"), text(json, "communityRoleCode"));
        String statusCode = firstNonNull(text(json, "statusCode"), text(json, "status"), text(json, "membershipStatusCode"));

        if (membershipId == null) throw new IllegalArgumentException("snapshot is missing membership id");
        if (tenantId == null) throw new IllegalArgumentException("snapshot is missing tenantId");
        if (communityId == null) throw new IllegalArgumentException("snapshot is missing communityId");
        if (userId == null) throw new IllegalArgumentException("snapshot is missing userId");
        if (roleCode == null) throw new IllegalArgumentException("snapshot is missing roleCode");
        if (statusCode == null) throw new IllegalArgumentException("snapshot is missing statusCode");

        // Compute deterministic labels from catalog VOs
        CommunityRoleValue role = CommunityRoleValue.of(roleCode);
        MembershipStatusValue status = MembershipStatusValue.of(statusCode);

        return new MembershipEventPayloadV1(
                membershipId,
                tenantId,
                communityId,
                userId,

                role.getCode(),
                role.getLabel(),

                status.getCode(),
                status.getLabel(),

                instant(json, "since"),
                instant(json, "deactivatedAt"),

                instant(json, "createdAt"),
                instant(json, "updatedAt")
        );
    }

    // ----------------- parsing helpers -----------------

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

    private static String firstNonNull(String... values) {
        if (values == null) return null;
        for (String v : values) {
            if (v != null) return v;
        }
        return null;
    }
}