package com.paravai.communities.community.domain.model;

import com.paravai.communities.community.domain.value.CommunityRulesValue;
import com.paravai.communities.community.domain.value.CommunityStatusValue;
import com.paravai.communities.community.domain.value.CommunityVisibilityValue;
import com.paravai.foundation.domain.value.IdValue;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root: Community
 *
 * Conventions:
 * - All IDs are IdValue
 * - Optional informational text fields use String (no dedicated VO)
 * - Instances are created ONLY via CommunityFactory (never via constructors)
 * - recreate() is used for rehydration from persistence
 *
 * Aggregate boundary:
 * - Community does NOT own Memberships (Membership is a separate aggregate).
 */
public final class Community {

    private final IdValue id;
    private final IdValue tenantId;

    private String name;
    private String slug;

    private String description; // optional, informational

    private CommunityVisibilityValue visibility;

    private CommunityRulesValue rules; // optional until A3

    private CommunityStatusValue status; // ACTIVE | ARCHIVED (catalog VO)
    private Instant archivedAt;          // present only if ARCHIVED

    private final IdValue createdBy;

    private final Instant createdAt;
    private Instant updatedAt;

    // -------------------------------------------------
    // Constructor (package-private) — only Factory can call
    // -------------------------------------------------
    Community(IdValue id,
              IdValue tenantId,
              String name,
              String slug,
              String description,
              CommunityVisibilityValue visibility,
              CommunityRulesValue rules,
              CommunityStatusValue status,
              Instant archivedAt,
              IdValue createdBy,
              Instant createdAt,
              Instant updatedAt,
              boolean validate) {

        this.id = Objects.requireNonNull(id, "Community id is required");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant id is required");

        this.name = requireNonBlank(name, "Community name is required");
        this.slug = requireNonBlank(slug, "Community slug is required");

        this.description = normalizeOptional(description);

        this.visibility = Objects.requireNonNull(visibility, "Community visibility is required");
        this.rules = rules; // optional

        this.status = Objects.requireNonNull(status, "Community status is required");
        this.archivedAt = archivedAt;

        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");

        if (validate) {
            validateInvariants();
        }
    }

    // -------------------------------------------------
    // Domain behavior
    // -------------------------------------------------

    public void rename(String newName) {
        this.name = requireNonBlank(newName, "New name is required");
        this.slug = SlugUtil.toSlug(newName);
        touch();
        validateInvariants();
    }

    /**
     * A2 - Change visibility (PUBLIC/PRIVATE).
     * Idempotent: if same value, no-op.
     */
    public void changeVisibility(CommunityVisibilityValue newVisibility) {
        Objects.requireNonNull(newVisibility, "Visibility is required");

        if (this.visibility.equals(newVisibility)) {
            return; // idempotent no-op
        }

        this.visibility = newVisibility;
        touch();
        validateInvariants();
    }

    /**
     * A3 - Define rules + allowed exchange types.
     * Idempotent: if same value, no-op.
     */
    public void defineRules(CommunityRulesValue newRules) {
        Objects.requireNonNull(newRules, "Rules are required");

        if (Objects.equals(this.rules, newRules)) {
            return; // idempotent no-op
        }

        this.rules = newRules;
        touch();
        validateInvariants();
    }

    public void clearRules() {
        if (this.rules == null) return;
        this.rules = null;
        touch();
        validateInvariants();
    }

    public void archive(Instant when) {
        if (isArchived()) return;

        this.status = CommunityStatusValue.of("ARCHIVED");
        this.archivedAt = (when != null ? when : Instant.now());
        touch();
        validateInvariants();
    }

    public void restore() {
        if (isActive()) return;

        this.status = CommunityStatusValue.of("ACTIVE");
        this.archivedAt = null;
        touch();
        validateInvariants();
    }

    // -------------------------------------------------
    // Invariants
    // -------------------------------------------------

    private void validateInvariants() {
        // timestamps
        if (createdAt.isAfter(updatedAt)) {
            throw new IllegalStateException("createdAt cannot be after updatedAt");
        }

        // status <-> archivedAt consistency
        if (isArchived() && archivedAt == null) {
            throw new IllegalStateException("Archived community must have archivedAt");
        }
        if (isActive() && archivedAt != null) {
            throw new IllegalStateException("Active community cannot have archivedAt");
        }

        // rules constraints (A3)
        // (CommunityRulesValue already enforces non-empty allowedExchangeTypes,
        //  but we keep this defensive check for invariants readability.)
        if (rules != null && rules.getAllowedExchangeTypes().isEmpty()) {
            throw new IllegalStateException("allowedExchangeTypes must not be empty");
        }

        // name/slug non-blank
        requireNonBlank(name, "Community name is required");
        requireNonBlank(slug, "Community slug is required");
    }

    private boolean isArchived() {
        return "ARCHIVED".equals(status.getCode());
    }

    private boolean isActive() {
        return "ACTIVE".equals(status.getCode());
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    // -------------------------------------------------
    // Getters (no setters)
    // -------------------------------------------------

    public IdValue id() { return id; }
    public IdValue tenantId() { return tenantId; }

    public String name() { return name; }
    public String slug() { return slug; }

    public Optional<String> description() { return Optional.ofNullable(description); }

    public CommunityVisibilityValue visibility() { return visibility; }
    public Optional<CommunityRulesValue> rules() { return Optional.ofNullable(rules); }

    public CommunityStatusValue status() { return status; }
    public Optional<Instant> archivedAt() { return Optional.ofNullable(archivedAt); }

    public IdValue createdBy() { return createdBy; }

    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    // -------------------------------------------------
    // Identity equality
    // -------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Community that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return "Community{id=%s, tenantId=%s, name=%s, slug=%s, visibility=%s, status=%s}"
                .formatted(id.value(), tenantId.value(), name, slug, visibility, status);
    }

    // -------------------------------------------------
    // Helpers
    // -------------------------------------------------

    static String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    static String normalizeOptional(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Minimal deterministic slug algorithm for MVP.
     * Replace with shared util if you already have one.
     */
    static final class SlugUtil {
        private SlugUtil() {}

        static String toSlug(String name) {
            String base = requireNonBlank(name, "Name is required for slug generation");
            String s = base.toLowerCase(Locale.ROOT).trim();
            s = s.replaceAll("\\s+", "-");
            s = s.replaceAll("[^a-z0-9\\-]", "");
            s = s.replaceAll("\\-+", "-");
            s = s.replaceAll("(^\\-)|(\\-$)", "");
            if (s.isBlank()) {
                throw new IllegalArgumentException("Slug cannot be derived from name");
            }
            return s;
        }
    }
}