package com.paravai.communities.membership.infrastructure.persistence.mongo.document;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.model.MembershipFactory;
import com.paravai.communities.membership.domain.value.CommunityRoleValue;
import com.paravai.communities.membership.domain.value.MembershipStatusValue;
import com.paravai.foundation.domain.value.IdValue;
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

    private String roleCode;   // catalog code
    private String statusCode; // catalog code

    private Instant since;
    private Instant deactivatedAt;

    private Instant createdAt;
    private Instant updatedAt;

    private int documentVersion = DOCUMENT_VERSION;

    // -------------------------
    // Mapping
    // -------------------------

    public static MembershipDocument fromDomain(Membership m) {
        MembershipDocument d = new MembershipDocument();

        d.id = m.id().value();

        d.tenantId = m.tenantId().value();
        d.communityId = m.communityId().value();
        d.userId = m.userId().value();

        d.roleCode = m.role().getCode();
        d.statusCode = m.status().getCode();

        d.since = m.since();
        d.deactivatedAt = m.deactivatedAt();

        d.createdAt = m.createdAt();
        d.updatedAt = m.updatedAt();

        return d;
    }

    public Membership toDomain() {
        if (documentVersion < DOCUMENT_VERSION) {
            log.warn("Reading older Membership document version {}", documentVersion);
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
        if (roleCode == null || roleCode.isBlank()) {
            throw new IllegalStateException("Invalid Membership document: roleCode is required");
        }
        if (statusCode == null || statusCode.isBlank()) {
            throw new IllegalStateException("Invalid Membership document: statusCode is required");
        }
        if (since == null) {
            throw new IllegalStateException("Invalid Membership document: since is required");
        }
        if (createdAt == null) {
            throw new IllegalStateException("Invalid Membership document: createdAt is required");
        }
        if (updatedAt == null) {
            throw new IllegalStateException("Invalid Membership document: updatedAt is required");
        }

        return MembershipFactory.recreate(
                IdValue.of(id),
                IdValue.of(tenantId),
                IdValue.of(communityId),
                IdValue.of(userId),
                CommunityRoleValue.of(roleCode),
                MembershipStatusValue.of(statusCode),
                since,
                deactivatedAt,
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

    public String getCommunityId() { return communityId; }
    public void setCommunityId(String communityId) { this.communityId = communityId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getStatusCode() { return statusCode; }
    public void setStatusCode(String statusCode) { this.statusCode = statusCode; }

    public Instant getSince() { return since; }
    public void setSince(Instant since) { this.since = since; }

    public Instant getDeactivatedAt() { return deactivatedAt; }
    public void setDeactivatedAt(Instant deactivatedAt) { this.deactivatedAt = deactivatedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public int getDocumentVersion() { return documentVersion; }
    public void setDocumentVersion(int documentVersion) { this.documentVersion = documentVersion; }
}