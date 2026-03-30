package com.paravai.communities.membership.infrastructure.persistence.mongo.document;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.model.MembershipFactory;
import com.paravai.communities.membership.domain.value.CommunityRoleValue;
import com.paravai.communities.membership.domain.value.MembershipStatusValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.domain.value.TimestampValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("memberships")
@CompoundIndex(
        name = "ux_membership_tenant_community_user",
        def = "{'tenantId': 1, 'communityId': 1, 'userId': 1}",
        unique = true
)
public class MembershipDocument {

    public static final int DOCUMENT_VERSION = 1;
    private static final Logger log = LoggerFactory.getLogger(MembershipDocument.class);

    @Id
    private String id;

    private String tenantId;
    private String communityId;
    private String userId;

    /**
     * Nullable by design:
     * - ACTIVE   -> roleCode required
     * - PENDING  -> roleCode null
     * - REJECTED -> roleCode null
     */
    private String roleCode;

    private String statusCode;

    /**
     * Optional reason for rejected membership.
     * Only meaningful when statusCode == REJECTED.
     */
    private String rejectionReason;

    /**
     * Request creation timestamp.
     */
    private Instant requestedAt;

    /**
     * Decision timestamp:
     * - null for PENDING
     * - non-null for ACTIVE / REJECTED
     */
    private Instant decidedAt;

    private Instant createdAt;
    private Instant updatedAt;

    private int documentVersion = DOCUMENT_VERSION;

    // -------------------------
    // Mapping
    // -------------------------

    public static MembershipDocument fromDomain(Membership membership) {
        MembershipDocument document = new MembershipDocument();

        document.id = membership.id().value();

        document.tenantId = membership.tenantId().value();
        document.communityId = membership.communityId().value();
        document.userId = membership.userId().value();

        document.roleCode = membership.role().map(CommunityRoleValue::getCode).orElse(null);
        document.statusCode = membership.status().getCode();
        document.rejectionReason = membership.rejectionReason().orElse(null);

        document.requestedAt = membership.requestedAt().getInstant();
        document.decidedAt = membership.decidedAt().map(TimestampValue::getInstant).orElse(null);

        document.createdAt = membership.createdAt().getInstant();
        document.updatedAt = membership.updatedAt().getInstant();

        document.documentVersion = DOCUMENT_VERSION;

        return document;
    }

    public Membership toDomain() {
        if (documentVersion < DOCUMENT_VERSION) {
            log.warn("Reading older Membership document version {}", documentVersion);
        }

        validateStructure();

        return MembershipFactory.recreate(
                IdValue.of(id),
                IdValue.of(tenantId),
                IdValue.of(communityId),
                IdValue.of(userId),
                roleCode != null && !roleCode.isBlank() ? CommunityRoleValue.of(roleCode) : null,
                MembershipStatusValue.of(statusCode),
                rejectionReason,
                TimestampValue.of(requestedAt),
                decidedAt != null ? TimestampValue.of(decidedAt) : null,
                TimestampValue.of(createdAt),
                TimestampValue.of(updatedAt)
        );
    }

    private void validateStructure() {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Invalid Membership document: id is required");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalStateException("Invalid Membership document: tenantId is required");
        }
        if (communityId == null || communityId.isBlank()) {
            throw new IllegalStateException("Invalid Membership document: communityId is required");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Invalid Membership document: userId is required");
        }
        if (statusCode == null || statusCode.isBlank()) {
            throw new IllegalStateException("Invalid Membership document: statusCode is required");
        }
        if (requestedAt == null) {
            throw new IllegalStateException("Invalid Membership document: requestedAt is required");
        }
        if (createdAt == null) {
            throw new IllegalStateException("Invalid Membership document: createdAt is required");
        }
        if (updatedAt == null) {
            throw new IllegalStateException("Invalid Membership document: updatedAt is required");
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
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