package com.dekra.service.regulations.standards.domain.model;

import com.dekra.service.foundation.domaincore.value.DateValue;
import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import org.bouncycastle.util.Times;

import java.util.Objects;

public final class ApplicabilityContext {

    private final IdValue id;

    private final IdValue certificationSchemeId;

    private final DateValue effectiveDate;     // mandatory
    private final DateValue endOfValidityDate; // optional (nullable)
    private final TimestampValue createdAt;

    private ApplicabilityContext(IdValue id,
                                 IdValue certificationSchemeId,
                                 DateValue effectiveDate,
                                 DateValue endOfValidityDate,
                                 TimestampValue createdAt){

        this.id = Objects.requireNonNull(id, "id");
        this.certificationSchemeId = Objects.requireNonNull(certificationSchemeId, "certificationSchemeId");
        this.effectiveDate = Objects.requireNonNull(effectiveDate, "effectiveDate");
        this.endOfValidityDate = endOfValidityDate;
        this.createdAt = Objects.requireNonNull(createdAt);

        validateInvariants();
    }

    public static ApplicabilityContext create(IdValue id,
                                              IdValue certificationSchemeId,
                                              DateValue effectiveDate,
                                              DateValue endOfValidityDate,
                                              TimestampValue now) {
        return new ApplicabilityContext(id, certificationSchemeId, effectiveDate, endOfValidityDate,now);
    }

    private void validateInvariants() {
        // INV-11: if end date defined, it must be later than effective date (strictly after)
        if (endOfValidityDate != null && !endOfValidityDate.getDate().isAfter(effectiveDate.getDate())) {
            throw new IllegalStateException("End of validity date must be later than effective date");
        }
    }

    // Getters
    public IdValue id() { return id; }
    public IdValue certificationSchemeId() { return certificationSchemeId; }
    public DateValue effectiveDate() { return effectiveDate; }
    public DateValue endOfValidityDate() { return endOfValidityDate; }
    public TimestampValue createdAt() {return createdAt;}

    /**
     * Helper to simplify overlap checks.
     * Open-ended is treated as +infinity.
     */
    public boolean overlaps(ApplicabilityContext other) {
        Objects.requireNonNull(other, "other");
        if (!this.certificationSchemeId.equals(other.certificationSchemeId)) return false;

        var aStart = this.effectiveDate.getDate();
        var aEnd   = this.endOfValidityDate == null ? null : this.endOfValidityDate.getDate();

        var bStart = other.effectiveDate.getDate();
        var bEnd   = other.endOfValidityDate == null ? null : other.endOfValidityDate.getDate();

        // [start, end] overlap, where null end = infinity
        boolean endsBefore = (aEnd != null) && aEnd.isBefore(bStart);
        boolean otherEndsBefore = (bEnd != null) && bEnd.isBefore(aStart);

        return !(endsBefore || otherEndsBefore);
    }
}
