package com.paravai.communities.community.infrastructure.persistence.mongo.document;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.model.CommunityFactory;
import com.paravai.communities.community.domain.value.CommunityRulesValue;
import com.paravai.communities.community.domain.value.CommunityStatusValue;
import com.paravai.communities.community.domain.value.CommunityVisibilityValue;
import com.paravai.communities.community.domain.value.ExchangeTypeValue;
import com.paravai.foundation.domain.value.IdValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Document("communities")
@CompoundIndex(
        name = "ux_community_tenant_slug",
        def = "{'tenantId': 1, 'slug': 1}",
        unique = true
)
public class CommunityDocument {

    public static final int DOCUMENT_VERSION = 1;
    private static final Logger log = LoggerFactory.getLogger(CommunityDocument.class);

    @Id
    private String id;

    private String tenantId;

    private String name;
    private String slug;

    private String description;

    private String visibilityCode; // catalog code

    private String statusCode; // catalog code
    private Instant archivedAt;

    private String createdBy;

    private Instant createdAt;
    private Instant updatedAt;

    // Rules (optional)
    private String rulesText;
    private List<String> allowedExchangeTypeCodes; // catalog codes

    private int documentVersion = DOCUMENT_VERSION;

    // -------------------------
    // Mapping
    // -------------------------

    public static CommunityDocument fromDomain(Community c) {
        CommunityDocument d = new CommunityDocument();

        d.id = c.id().value();
        d.tenantId = c.tenantId().value();

        d.name = c.name();
        d.slug = c.slug();

        d.description = c.description().orElse(null);

        d.visibilityCode = c.visibility().getCode();

        d.statusCode = c.status().getCode();
        d.archivedAt = c.archivedAt().orElse(null);

        d.createdBy = c.createdBy().value();

        d.createdAt = c.createdAt();
        d.updatedAt = c.updatedAt();

        if (c.rules().isPresent()) {
            CommunityRulesValue rules = c.rules().get();
            d.rulesText = rules.getText();
            d.allowedExchangeTypeCodes = rules.getAllowedExchangeTypes().stream()
                    .map(ExchangeTypeValue::getCode)
                    .toList();
        } else {
            d.rulesText = null;
            d.allowedExchangeTypeCodes = null;
        }

        return d;
    }

    public Community toDomain() {
        if (documentVersion < DOCUMENT_VERSION) {
            log.warn("Reading older Community document version {}", documentVersion);
        }

        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Invalid Community document: tenantId is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Invalid Community document: name is required");
        }
        if (slug == null || slug.isBlank()) {
            throw new IllegalStateException("Invalid Community document: slug is required");
        }
        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalStateException("Invalid Community document: createdBy is required");
        }
        if (visibilityCode == null || visibilityCode.isBlank()) {
            throw new IllegalStateException("Invalid Community document: visibilityCode is required");
        }
        if (statusCode == null || statusCode.isBlank()) {
            throw new IllegalStateException("Invalid Community document: statusCode is required");
        }
        if (createdAt == null) {
            throw new IllegalStateException("Invalid Community document: createdAt is required");
        }
        if (updatedAt == null) {
            throw new IllegalStateException("Invalid Community document: updatedAt is required");
        }

        CommunityRulesValue rules = null;
        if (allowedExchangeTypeCodes != null && !allowedExchangeTypeCodes.isEmpty()) {
            Set<ExchangeTypeValue> allowed = allowedExchangeTypeCodes.stream()
                    .map(ExchangeTypeValue::of)
                    .collect(java.util.stream.Collectors.toSet());

            rules = CommunityRulesValue.of(rulesText, allowed);
        } else if (rulesText != null && !rulesText.isBlank()) {
            // If text exists but no allowed types, the document is inconsistent.
            throw new IllegalStateException("Invalid Community document: rulesText present but allowedExchangeTypeCodes is empty");
        }

        return CommunityFactory.recreate(
                IdValue.of(id),
                IdValue.of(tenantId),
                name,
                slug,
                description,
                CommunityVisibilityValue.of(visibilityCode),
                rules,
                CommunityStatusValue.of(statusCode),
                archivedAt,
                IdValue.of(createdBy),
                createdAt,
                updatedAt
        );
    }

    // -------------------------
    // Getters/Setters
    // -------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVisibilityCode() { return visibilityCode; }
    public void setVisibilityCode(String visibilityCode) { this.visibilityCode = visibilityCode; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public Instant getArchivedAt() { return archivedAt; }
    public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getRulesText() { return rulesText; }
    public void setRulesText(String rulesText) { this.rulesText = rulesText; }

    public List<String> getAllowedExchangeTypeCodes() { return allowedExchangeTypeCodes; }
    public void setAllowedExchangeTypeCodes(List<String> allowedExchangeTypeCodes) { this.allowedExchangeTypeCodes = allowedExchangeTypeCodes; }

    public int getDocumentVersion() { return documentVersion; }
    public void setDocumentVersion(int documentVersion) { this.documentVersion = documentVersion; }
}