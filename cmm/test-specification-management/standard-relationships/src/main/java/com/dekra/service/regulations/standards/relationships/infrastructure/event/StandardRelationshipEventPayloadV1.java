package com.paravai.regulations.standards.relationships.infrastructure.event;

import com.paravai.regulations.standards.relationships.domain.model.StandardRelationship;
import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;
import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;
import com.paravai.regulations.standards.relationships.domain.value.StandardVersionRefValue;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Objects;

public record StandardRelationshipEventPayloadV1(

        String relationshipId,

        String fromStandardId,
        String fromVersionId,

        String toStandardId,
        String toVersionId,

        String typeCode,
        String typeLabel,

        String purposeCode,   // nullable
        String purposeLabel,  // nullable

        Instant createdAt
) {

    // --------------------------------------------------
    // Domain factory
    // --------------------------------------------------

    public static StandardRelationshipEventPayloadV1 from(StandardRelationship r) {

        StandardVersionRefValue from = r.from();
        StandardVersionRefValue to = r.to();

        StandardRelationshipTypeValue type = r.type();
        StandardRelationshipPurposeValue purpose = r.purpose(); // may be null

        return new StandardRelationshipEventPayloadV1(
                r.id().getValue(),

                from.standardId().getValue(),
                from.versionId().getValue(),

                to.standardId().getValue(),
                to.versionId().getValue(),

                type.getCode(),
                type.getLabel(),

                purpose != null ? purpose.getCode() : null,
                purpose != null ? purpose.getLabel() : null,

                r.createdAt().getInstant()
        );
    }

    // --------------------------------------------------
    // Snapshot factory
    // --------------------------------------------------

    public static StandardRelationshipEventPayloadV1 fromSnapshot(JsonNode json) {

        if (json == null || json.isNull()) {
            throw new IllegalArgumentException("snapshot json cannot be null");
        }

        String relationshipId = firstNonNull(
                text(json, "id"),
                text(json, "relationshipId")
        );

        String fromStandardId = text(json, "fromStandardId");
        String fromVersionId = text(json, "fromVersionId");

        String toStandardId = text(json, "toStandardId");
        String toVersionId = text(json, "toVersionId");

        String typeCode = text(json, "typeCode");
        if (typeCode == null) {
            throw new IllegalArgumentException("snapshot is missing typeCode");
        }

        StandardRelationshipTypeValue type = StandardRelationshipTypeValue.of(typeCode);

        String purposeCode = text(json, "purposeCode");
        StandardRelationshipPurposeValue purpose =
                purposeCode != null ? StandardRelationshipPurposeValue.of(purposeCode) : null;

        return new StandardRelationshipEventPayloadV1(
                relationshipId,

                fromStandardId,
                fromVersionId,

                toStandardId,
                toVersionId,

                type.getCode(),
                type.getLabel(),

                purpose != null ? purpose.getCode() : null,
                purpose != null ? purpose.getLabel() : null,

                instant(json, "createdAt")
        );
    }

    // --------------------------------------------------
    // Helpers
    // --------------------------------------------------

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
