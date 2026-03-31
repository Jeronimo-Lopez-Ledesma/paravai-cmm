package com.paravai.communities.offer.infrastructure.persistence.mongo.document;

import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.domain.model.OfferFactory;
import com.paravai.communities.offer.domain.value.ExchangeTypeValue;
import com.paravai.communities.offer.domain.value.OfferStatusValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("offers")
@CompoundIndex(
        name = "ux_offer_tenant_community_resource_status",
        def = "{'tenantId': 1, 'communityId': 1, 'resourceId': 1, 'statusCode': 1}",
        unique = true
)
public class OfferDocument {

    public static final int DOCUMENT_VERSION = 1;
    private static final Logger log = LoggerFactory.getLogger(OfferDocument.class);

    @Id
    private String id;

    private String tenantId;
    private String communityId;
    private String resourceId;
    private String ownerId;

    private String exchangeTypeCode;
    private String description;
    private String statusCode;

    private Instant createdAt;
    private Instant updatedAt;

    private int documentVersion = DOCUMENT_VERSION;

    // -------------------------
    // Mapping
    // -------------------------

    public static OfferDocument fromDomain(Offer offer) {
        OfferDocument document = new OfferDocument();

        document.id = offer.id().value();

        document.tenantId = offer.tenantId().value();
        document.communityId = offer.communityId().value();
        document.resourceId = offer.resourceId().value();
        document.ownerId = offer.ownerId().value();

        document.exchangeTypeCode = offer.exchangeType().value();
        document.description = offer.description().orElse(null);
        document.statusCode = offer.status().value();

        document.createdAt = offer.createdAt().getInstant();
        document.updatedAt = offer.updatedAt().getInstant();

        document.documentVersion = DOCUMENT_VERSION;

        return document;
    }

    public Offer toDomain() {
        if (documentVersion < DOCUMENT_VERSION) {
            log.warn("Reading older Offer document version {}", documentVersion);
        }

        validateStructure();

        return OfferFactory.recreate(
                IdValue.of(id),
                IdValue.of(tenantId),
                IdValue.of(communityId),
                IdValue.of(resourceId),
                IdValue.of(ownerId),
                ExchangeTypeValue.of(exchangeTypeCode),
                description,
                OfferStatusValue.of(statusCode),
                TimestampValue.of(createdAt),
                TimestampValue.of(updatedAt)
        );
    }

    private void validateStructure() {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Invalid Offer document: id is required");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Invalid Offer document: tenantId is required");
        }
        if (communityId == null || communityId.isBlank()) {
            throw new IllegalStateException("Invalid Offer document: communityId is required");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalStateException("Invalid Offer document: resourceId is required");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalStateException("Invalid Offer document: ownerId is required");
        }
        if (exchangeTypeCode == null || exchangeTypeCode.isBlank()) {
            throw new IllegalStateException("Invalid Offer document: exchangeTypeCode is required");
        }
        if (statusCode == null || statusCode.isBlank()) {
            throw new IllegalStateException("Invalid Offer document: statusCode is required");
        }
        if (createdAt == null) {
            throw new IllegalStateException("Invalid Offer document: createdAt is required");
        }
        if (updatedAt == null) {
            throw new IllegalStateException("Invalid Offer document: updatedAt is required");
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

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getExchangeTypeCode() {
        return exchangeTypeCode;
    }

    public void setExchangeTypeCode(String exchangeTypeCode) {
        this.exchangeTypeCode = exchangeTypeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
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