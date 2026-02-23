package com.paravai.regulations.standards.infrastructure.event;

import com.paravai.foundation.domain.organization.value.OrganizationAssociationValue;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.model.StandardVersion;
import com.paravai.regulations.standards.domain.value.StandardTypeValue;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record StandardEventPayloadV1(
        String standardId,
        String code,
        String title,
        String description,
        String standardTypeCode,
        String standardTypeLabel,
        String issuingBodyOrganizationId,
        Instant createdAt,
        Instant updatedAt,
        List<StandardVersionPayloadV1> versions
) {

    public static StandardEventPayloadV1 from(Standard s) {
        OrganizationAssociationValue issuingBody = s.issuingBody(); // mandatory

        return new StandardEventPayloadV1(
                s.id().toString(),
                s.code().value(),
                s.title().value(),
                s.description(),
                s.type().getCode(),
                s.type().getLabel(),
                issuingBody.getOrganizationId().getValue(),
                s.createdAt().getInstant(),
                s.updatedAt().getInstant(),
                s.versions().stream().map(StandardVersionPayloadV1::from).toList()
        );
    }

    /**
     * Snapshot-based factory.
     * Reads the JSON produced by SnapshotMapper (aligned with StandardDocument).
     */
    public static StandardEventPayloadV1 fromSnapshot(JsonNode json) {
        if (json == null || json.isNull()) {
            throw new IllegalArgumentException("snapshot json cannot be null");
        }

        String id = firstNonNull(text(json, "id"), text(json, "standardId"));

        String issuingBodyOrgId = firstNonNull(
                text(json, "issuingBodyOrganizationId"),
                text(json, "issuingBodyOrgId")
        );
        if (issuingBodyOrgId == null) {
            throw new IllegalArgumentException("snapshot is missing issuingBodyOrganizationId");
        }

        // NEW: stored at document level as a simple string
        String typeCode = firstNonNull(
                text(json, "standardTypeCode"),
                text(json, "typeCode") // tolerate accidental legacy naming
        );
        if (typeCode == null) {
            throw new IllegalArgumentException("snapshot is missing standardTypeCode");
        }

        // Compute deterministic default label from catalog VO
        StandardTypeValue type = StandardTypeValue.of(typeCode);

        List<StandardVersionPayloadV1> versions = readVersions(json.path("versions"));

        return new StandardEventPayloadV1(
                id,
                text(json, "code"),
                text(json, "title"),
                text(json, "description"),
                type.getCode(),              // NEW
                type.getLabel(),             // NEW
                issuingBodyOrgId,
                instant(json, "createdAt"),
                instant(json, "updatedAt"),
                versions
        );
    }

    public record StandardVersionPayloadV1(
            String id,
            String version,
            String versionKey,        // NEW (optional but strongly recommended)
            String publicationDate,   // ISO date (nullable)
            String visibility,
            String statusCode,        // NEW
            String description,
            List<ApplicabilityContextPayloadV1> applicabilityContexts // NEW
    ) {
        public static StandardVersionPayloadV1 from(StandardVersion v) {
            return new StandardVersionPayloadV1(
                    v.id().toString(),
                    v.version().value(),
                    v.version().normalizedKey(), // versionKey
                    v.publicationDate() != null ? v.publicationDate().value().toString() : null,
                    v.visibility().value(),
                    v.status().getCode(),
                    v.description(),
                    v.applicabilityContexts() != null
                            ? v.applicabilityContexts().stream().map(ApplicabilityContextPayloadV1::from).toList()
                            : List.of()
            );
        }
    }

    public record ApplicabilityContextPayloadV1(
            String id,
            String certificationSchemeId,
            String effectiveDate,     // ISO date
            String endOfValidityDate  // ISO date (nullable)
    ) {
        public static ApplicabilityContextPayloadV1 from(
                com.paravai.regulations.standards.domain.model.ApplicabilityContext c
        ) {
            return new ApplicabilityContextPayloadV1(
                    c.id().toString(),
                    c.certificationSchemeId().toString(),
                    c.effectiveDate().toIsoString(),
                    c.endOfValidityDate() != null ? c.endOfValidityDate().toIsoString() : null
            );
        }
    }

    // ----------------- parsing helpers -----------------

    private static List<StandardVersionPayloadV1> readVersions(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();

        List<StandardVersionPayloadV1> out = new ArrayList<>();
        for (JsonNode v : arr) {

            List<ApplicabilityContextPayloadV1> contexts =
                    readApplicabilityContexts(v.path("applicabilityContexts"));

            out.add(new StandardVersionPayloadV1(
                    text(v, "id"),
                    text(v, "version"),
                    firstNonNull(text(v, "versionKey"), text(v, "normalizedKey")), // tolerate legacy
                    text(v, "publicationDate"),
                    text(v, "visibility"),
                    firstNonNull(text(v, "status"), text(v, "statusCode")), // snapshot uses "status" (document field)
                    text(v, "description"),
                    contexts
            ));
        }
        return List.copyOf(out);
    }

    private static List<ApplicabilityContextPayloadV1> readApplicabilityContexts(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();

        List<ApplicabilityContextPayloadV1> out = new ArrayList<>();
        for (JsonNode c : arr) {
            out.add(new ApplicabilityContextPayloadV1(
                    text(c, "id"),
                    firstNonNull(text(c, "certificationSchemeId"), text(c, "certificationSchemeRefId")),
                    text(c, "effectiveDate"),
                    firstNonNull(text(c, "endOfValidityDate"), text(c, "endDate"))
            ));
        }
        return List.copyOf(out);
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