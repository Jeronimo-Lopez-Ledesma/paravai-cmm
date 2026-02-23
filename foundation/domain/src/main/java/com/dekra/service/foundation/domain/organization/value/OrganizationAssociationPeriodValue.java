package com.dekra.service.foundation.domain.organization.value;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampRangesValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;

import java.util.Objects;
import java.util.Optional;

/**
 * OrganizationAssociationPeriodValue
 *
 * Weak, time-bounded association to an Organization and optionally an OrganizationLocation,
 * with audit timestamps.
 *
 * - Immutable
 * - Identity: (organizationId + organizationLocationId + target + startDate)
 * - Tracks createdAt / updatedAt
 */
public final class OrganizationAssociationPeriodValue {

    private final IdValue organizationId;
    private final IdValue organizationLocationId; // nullable
    private final OrganizationAssociationValue.AssociationTarget target;

    private final TimestampValue startDate;
    private final TimestampValue endDate; // nullable
    private final String comment;         // nullable

    private final TimestampValue createdAt;
    private final TimestampValue updatedAt;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Backward-friendly constructor (timestamps default to now()).
     */
    public OrganizationAssociationPeriodValue(
            IdValue organizationId,
            IdValue organizationLocationId,
            OrganizationAssociationValue.AssociationTarget target,
            TimestampValue startDate,
            TimestampValue endDate,
            String comment
    ) {
        this(
                organizationId,
                organizationLocationId,
                target,
                startDate,
                endDate,
                comment,
                TimestampValue.now(),
                TimestampValue.now()
        );
    }

    public OrganizationAssociationPeriodValue(
            IdValue organizationId,
            IdValue organizationLocationId,
            OrganizationAssociationValue.AssociationTarget target,
            TimestampValue startDate,
            TimestampValue endDate,
            String comment,
            TimestampValue createdAt,
            TimestampValue updatedAt
    ) {
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId");
        this.organizationLocationId = organizationLocationId;
        this.target = Objects.requireNonNull(target, "target");

        this.startDate = Objects.requireNonNullElseGet(startDate, TimestampValue::now);
        this.endDate = endDate;
        this.comment = comment;

        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");

        if (this.endDate != null && this.endDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("endDate must be >= startDate");
        }
        if (this.updatedAt.isBefore(this.createdAt)) {
            throw new IllegalArgumentException("updatedAt must be >= createdAt");
        }
    }

    // -------------------------------------------------------------------------
    // Static factories (recommended)
    // -------------------------------------------------------------------------

    public static OrganizationAssociationPeriodValue forServiceLocation(
            IdValue organizationId,
            IdValue organizationLocationId,
            TimestampValue startDate,
            TimestampValue endDate,
            String comment
    ) {
        return new OrganizationAssociationPeriodValue(
                organizationId,
                Objects.requireNonNull(organizationLocationId, "organizationLocationId"),
                OrganizationAssociationValue.AssociationTarget.SERVICE,
                startDate,
                endDate,
                comment
        );
    }

    public static OrganizationAssociationPeriodValue forServiceOrganization(
            IdValue organizationId,
            TimestampValue startDate,
            TimestampValue endDate,
            String comment
    ) {
        return new OrganizationAssociationPeriodValue(
                organizationId,
                null,
                OrganizationAssociationValue.AssociationTarget.SERVICE,
                startDate,
                endDate,
                comment
        );
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public IdValue getOrganizationId() {
        return organizationId;
    }

    public Optional<IdValue> getOrganizationLocationId() {
        return Optional.ofNullable(organizationLocationId);
    }

    public OrganizationAssociationValue.AssociationTarget getTarget() {
        return target;
    }

    public TimestampValue getStartDate() {
        return startDate;
    }

    public Optional<TimestampValue> getEndDate() {
        return Optional.ofNullable(endDate);
    }

    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }

    public TimestampValue getCreatedAt() {
        return createdAt;
    }

    public TimestampValue getUpdatedAt() {
        return updatedAt;
    }

    // -------------------------------------------------------------------------
    // Derived helpers
    // -------------------------------------------------------------------------

    public boolean belongsToLocation() {
        return organizationLocationId != null;
    }

    /** Optional: expose the "simple" association as a derived value (no state) */
    public OrganizationAssociationValue asAssociation() {
        return OrganizationAssociationValue.of(organizationId, organizationLocationId, target);
    }

    public boolean isActive() {
        return endDate == null || endDate.isAfterNow();
    }

    // -------------------------------------------------------------------------
    // Domain transitions (immutability preserved)
    // -------------------------------------------------------------------------

    public OrganizationAssociationPeriodValue withEndDate(TimestampValue newEndDate) {
        return new OrganizationAssociationPeriodValue(
                this.organizationId,
                this.organizationLocationId,
                this.target,
                this.startDate,
                Objects.requireNonNull(newEndDate),
                this.comment,
                this.createdAt,
                TimestampValue.now()
        );
    }

    public OrganizationAssociationPeriodValue withComment(String newComment) {
        return new OrganizationAssociationPeriodValue(
                this.organizationId,
                this.organizationLocationId,
                this.target,
                this.startDate,
                this.endDate,
                newComment,
                this.createdAt,
                TimestampValue.now()
        );
    }

    public OrganizationAssociationPeriodValue close(TimestampValue endDate) {
        return withEndDate(endDate);
    }

    // -------------------------------------------------------------------------
    // Business rules
    // -------------------------------------------------------------------------

    public boolean overlapsWith(OrganizationAssociationPeriodValue other) {
        Objects.requireNonNull(other, "other");

        // comparable only if same "association key" (org + loc + target)
        if (!Objects.equals(this.organizationId, other.organizationId)) return false;
        if (!Objects.equals(this.organizationLocationId, other.organizationLocationId)) return false;
        if (this.target != other.target) return false;

        return TimestampRangesValue.overlaps(
                this.startDate,
                this.endDate,
                other.startDate,
                other.endDate
        );
    }

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrganizationAssociationPeriodValue other)) return false;

        // Identity: (orgId + orgLocId + target + startDate)
        return organizationId.equals(other.organizationId)
                && Objects.equals(organizationLocationId, other.organizationLocationId)
                && target == other.target
                && startDate.equals(other.startDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationId, organizationLocationId, target, startDate);
    }

    @Override
    public String toString() {
        return "OrganizationAssociationPeriodValue{" +
                "organizationId=" + organizationId +
                ", organizationLocationId=" + (organizationLocationId != null ? organizationLocationId : "none") +
                ", target=" + target +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", comment=" + comment +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
