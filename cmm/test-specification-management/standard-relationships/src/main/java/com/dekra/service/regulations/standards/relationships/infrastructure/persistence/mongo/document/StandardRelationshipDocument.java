package com.dekra.service.regulations.standards.relationships.infrastructure.persistence.mongo.document;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationship;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationshipFactory;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;
import com.dekra.service.regulations.standards.relationships.domain.value.StandardVersionRefValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("standardRelationships")
@CompoundIndex(
        name = "ux_relationship_from_type_purpose_to",
        def = "{'fromStandardId': 1, 'fromVersionId': 1, 'typeCode': 1, 'purposeCode': 1, 'toStandardId': 1, 'toVersionId': 1}",
        unique = true
)
public class StandardRelationshipDocument {

    public static final int DOCUMENT_VERSION = 1;
    private static final Logger log = LoggerFactory.getLogger(StandardRelationshipDocument.class);

    @Id
    private String id;

    // --- weak refs (persist as simple ids) ---
    private String fromStandardId;
    private String fromVersionId;

    private String toStandardId;
    private String toVersionId;

    // --- catalog codes ---
    private String typeCode;
    private String purposeCode; // nullable unless REFERS_TO

    private Instant createdAt;

    private int documentVersion = DOCUMENT_VERSION;

    // -------------------------
    // Mapping
    // -------------------------

    public static StandardRelationshipDocument fromDomain(StandardRelationship r) {
        StandardRelationshipDocument d = new StandardRelationshipDocument();

        d.id = r.id().getValue();

        d.fromStandardId = r.from().standardId().getValue();
        d.fromVersionId = r.from().versionId().getValue();

        d.toStandardId = r.to().standardId().getValue();
        d.toVersionId = r.to().versionId().getValue();

        d.typeCode = r.type().getCode();
        d.purposeCode = r.purpose() != null ? r.purpose().getCode() : null;

        d.createdAt = r.createdAt().getInstant();

        return d;
    }

    public StandardRelationship toDomain() {
        if (documentVersion < DOCUMENT_VERSION) {
            log.warn("Reading older StandardRelationship document version {}", documentVersion);
        }

        // --- required fields validation ---
        if (fromStandardId == null || fromStandardId.isBlank()) {
            throw new IllegalStateException("Invalid StandardRelationship document: fromStandardId is required");
        }
        if (fromVersionId == null || fromVersionId.isBlank()) {
            throw new IllegalStateException("Invalid StandardRelationship document: fromVersionId is required");
        }
        if (toStandardId == null || toStandardId.isBlank()) {
            throw new IllegalStateException("Invalid StandardRelationship document: toStandardId is required");
        }
        if (toVersionId == null || toVersionId.isBlank()) {
            throw new IllegalStateException("Invalid StandardRelationship document: toVersionId is required");
        }
        if (typeCode == null || typeCode.isBlank()) {
            throw new IllegalStateException("Invalid StandardRelationship document: typeCode is required");
        }
        if (createdAt == null) {
            throw new IllegalStateException("Invalid StandardRelationship document: createdAt is required");
        }

        StandardVersionRefValue fromRef = StandardVersionRefValue.of(
                IdValue.of(fromStandardId),
                IdValue.of(fromVersionId)
        );

        StandardVersionRefValue toRef = StandardVersionRefValue.of(
                IdValue.of(toStandardId),
                IdValue.of(toVersionId)
        );

        StandardRelationshipTypeValue type = StandardRelationshipTypeValue.of(typeCode);

        StandardRelationshipPurposeValue purpose = (purposeCode != null && !purposeCode.isBlank())
                ? StandardRelationshipPurposeValue.of(purposeCode)
                : null;

        return StandardRelationshipFactory.recreate(
                IdValue.of(id),
                fromRef,
                toRef,
                type,
                purpose,
                TimestampValue.of(createdAt)
        );
    }

    // -------------------------
    // Getters/Setters
    // -------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFromStandardId() { return fromStandardId; }
    public void setFromStandardId(String fromStandardId) { this.fromStandardId = fromStandardId; }

    public String getFromVersionId() { return fromVersionId; }
    public void setFromVersionId(String fromVersionId) { this.fromVersionId = fromVersionId; }

    public String getToStandardId() { return toStandardId; }
    public void setToStandardId(String toStandardId) { this.toStandardId = toStandardId; }

    public String getToVersionId() { return toVersionId; }
    public void setToVersionId(String toVersionId) { this.toVersionId = toVersionId; }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getPurposeCode() { return purposeCode; }
    public void setPurposeCode(String purposeCode) { this.purposeCode = purposeCode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public int getDocumentVersion() { return documentVersion; }
    public void setDocumentVersion(int documentVersion) { this.documentVersion = documentVersion; }
}
