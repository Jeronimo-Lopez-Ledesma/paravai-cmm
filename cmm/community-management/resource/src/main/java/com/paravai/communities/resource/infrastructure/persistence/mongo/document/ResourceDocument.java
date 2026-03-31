package com.paravai.communities.resource.infrastructure.persistence.mongo.document;

import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.domain.model.ResourceFactory;
import com.paravai.communities.resource.domain.value.ResourceConditionValue;
import com.paravai.communities.resource.domain.value.ResourceTitleValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("resources")
@CompoundIndex(
        name = "ix_resource_tenant_owner",
        def = "{'tenantId': 1, 'ownerId': 1}"
)
public class ResourceDocument {

    public static final int DOCUMENT_VERSION = 1;
    private static final Logger log = LoggerFactory.getLogger(ResourceDocument.class);

    @Id
    private String id;

    private String tenantId;
    private String ownerId;

    /**
     * Mandatory resource title.
     */
    private String title;

    /**
     * Optional free-text description.
     */
    private String description;

    /**
     * Optional catalog code:
     * - NEW
     * - LIKE_NEW
     * - GOOD
     * - FAIR
     * - POOR
     */
    private String conditionCode;

    private Instant createdAt;
    private Instant updatedAt;

    private int documentVersion = DOCUMENT_VERSION;

    // -------------------------
    // Mapping
    // -------------------------

    public static ResourceDocument fromDomain(Resource resource) {
        ResourceDocument document = new ResourceDocument();

        document.id = resource.id().value();
        document.tenantId = resource.tenantId().value();
        document.ownerId = resource.ownerId().value();

        document.title = resource.title().value();
        document.description = resource.description().orElse(null);
        document.conditionCode = resource.condition().map(ResourceConditionValue::value).orElse(null);

        document.createdAt = resource.createdAt().getInstant();
        document.updatedAt = resource.updatedAt().getInstant();

        document.documentVersion = DOCUMENT_VERSION;

        return document;
    }

    public Resource toDomain() {
        if (documentVersion < DOCUMENT_VERSION) {
            log.warn("Reading older Resource document version {}", documentVersion);
        }

        validateStructure();

        return ResourceFactory.recreate(
                IdValue.of(id),
                IdValue.of(tenantId),
                IdValue.of(ownerId),
                ResourceTitleValue.of(title),
                description,
                conditionCode != null && !conditionCode.isBlank() ? ResourceConditionValue.of(conditionCode) : null,
                TimestampValue.of(createdAt),
                TimestampValue.of(updatedAt)
        );
    }

    private void validateStructure() {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Invalid Resource document: id is required");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Invalid Resource document: tenantId is required");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalStateException("Invalid Resource document: ownerId is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalStateException("Invalid Resource document: title is required");
        }
        if (createdAt == null) {
            throw new IllegalStateException("Invalid Resource document: createdAt is required");
        }
        if (updatedAt == null) {
            throw new IllegalStateException("Invalid Resource document: updatedAt is required");
        }
    }

    // -------------------------
    // Getters / Setters
    // -------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(String conditionCode) {
        this.conditionCode = conditionCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(int documentVersion) {
        this.documentVersion = documentVersion;
    }
}