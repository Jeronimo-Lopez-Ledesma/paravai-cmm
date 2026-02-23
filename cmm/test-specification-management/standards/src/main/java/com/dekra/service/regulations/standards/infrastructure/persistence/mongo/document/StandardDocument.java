package com.dekra.service.regulations.standards.infrastructure.persistence.mongo.document;

import com.dekra.service.foundation.domain.organization.value.OrganizationAssociationValue;
import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.regulations.standards.domain.model.Standard;
import com.dekra.service.regulations.standards.domain.model.StandardFactory;
import com.dekra.service.regulations.standards.domain.model.StandardVersion;
import com.dekra.service.regulations.standards.domain.value.StandardCodeValue;
import com.dekra.service.regulations.standards.domain.value.StandardTitleValue;
import com.dekra.service.regulations.standards.domain.value.StandardTypeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("standards")
@CompoundIndex(
        name = "ux_standard_issuingBody_code",
        def = "{'issuingBodyOrganizationId': 1, 'codeKey': 1}",
        unique = true
)
public class StandardDocument {

    public static final int DOCUMENT_VERSION = 1;
    private static final Logger log = LoggerFactory.getLogger(StandardDocument.class);

    @Id
    private String id;

    private String code;
    private String codeKey;

    private String title;
    private String description;

    private String standardTypeCode; // NEW (persist code only)

    private Instant createdAt;
    private Instant updatedAt;

    private List<StandardVersionDocument> versions;

    private String standardizationBodyId;

    private int documentVersion = DOCUMENT_VERSION;

    // -------------------------
    // Mapping
    // -------------------------

    public static StandardDocument fromDomain(Standard s) {
        StandardDocument d = new StandardDocument();

        d.id = s.id().getValue();

        d.code = s.code().value();
        d.codeKey = s.code().normalizedKey();

        d.title = s.title().value();
        d.description = s.description();

        d.standardTypeCode = s.type().getCode(); // NEW

        d.createdAt = s.createdAt().getInstant();
        d.updatedAt = s.updatedAt().getInstant();

        d.versions = s.versions().stream()
                .map(StandardVersionDocument::fromDomain)
                .toList();

        d.standardizationBodyId = s.issuingBody().getOrganizationId().getValue();

        return d;
    }

    public Standard toDomain() {
        if (documentVersion < DOCUMENT_VERSION) {
            log.warn("Reading older Standard document version {}", documentVersion);
        }

        List<StandardVersion> domainVersions = versions != null
                ? versions.stream().map(StandardVersionDocument::toDomain).toList()
                : List.of();

        if (standardizationBodyId == null || standardizationBodyId.isBlank()) {
            throw new IllegalStateException("Invalid Standard document: issuingBodyOrganizationId is required");
        }
        if (standardTypeCode == null || standardTypeCode.isBlank()) {
            throw new IllegalStateException("Invalid Standard document: standardTypeCode is required");
        }

        var issuingBody = OrganizationAssociationValue.forStandardIssuer(IdValue.of(standardizationBodyId));

        return StandardFactory.recreate(
                IdValue.of(id),
                StandardCodeValue.of(code),
                StandardTitleValue.of(title),
                StandardTypeValue.of(standardTypeCode), // NEW
                domainVersions,
                TimestampValue.of(createdAt),
                TimestampValue.of(updatedAt),
                description,
                issuingBody
        );
    }

    // -------------------------
    // Getters/Setters
    // -------------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCodeKey() { return codeKey; }
    public void setCodeKey(String codeKey) { this.codeKey = codeKey; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStandardTypeCode() { return standardTypeCode; }
    public void setStandardTypeCode(String standardTypeCode) { this.standardTypeCode = standardTypeCode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<StandardVersionDocument> getVersions() { return versions; }
    public void setVersions(List<StandardVersionDocument> versions) { this.versions = versions; }

    public String getStandardizationBodyId() { return standardizationBodyId; }
    public void setStandardizationBodyId(String standardizationBodyId) { this.standardizationBodyId = standardizationBodyId; }

    public int getDocumentVersion() { return documentVersion; }
    public void setDocumentVersion(int documentVersion) { this.documentVersion = documentVersion; }
}