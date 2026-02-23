package com.paravai.regulations.standards.infrastructure.persistence.mongo.document;

import com.paravai.foundation.domaincore.value.DateValue;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.domaincore.value.TimestampValue;
import com.paravai.regulations.standards.domain.model.ApplicabilityContext;

import java.time.Instant;
import java.time.LocalDate;

public class ApplicabilityContextDocument {

    private String id;

    private String certificationSchemeId;

    private LocalDate effectiveDate;       // mandatory
    private LocalDate endOfValidityDate;   // optional

    private Instant createdAt;

    // -------------------------
    // Mapping
    // -------------------------

    public static ApplicabilityContextDocument fromDomain(ApplicabilityContext c) {
        ApplicabilityContextDocument d = new ApplicabilityContextDocument();
        d.id = c.id().getValue();
        d.certificationSchemeId = c.certificationSchemeId().getValue();
        d.effectiveDate = c.effectiveDate().getDate();
        d.endOfValidityDate = c.endOfValidityDate() != null ? c.endOfValidityDate().getDate() : null;
        d.createdAt = Instant.now();

        return d;
    }

    public ApplicabilityContext toDomain() {
        if (certificationSchemeId == null || certificationSchemeId.isBlank()) {
            throw new IllegalStateException("Invalid ApplicabilityContext document: certificationSchemeId is required");
        }
        if (effectiveDate == null) {
            throw new IllegalStateException("Invalid ApplicabilityContext document: effectiveDate is required");
        }

        return ApplicabilityContext.create(
                IdValue.of(id),
                IdValue.of(certificationSchemeId),
                DateValue.of(effectiveDate),
                endOfValidityDate != null ? DateValue.of(endOfValidityDate) : null,
                TimestampValue.of(createdAt)

        );
    }

    // -------------------------
    // Getters/Setters
    // -------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCertificationSchemeId() { return certificationSchemeId; }
    public void setCertificationSchemeId(String certificationSchemeId) { this.certificationSchemeId = certificationSchemeId; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndOfValidityDate() { return endOfValidityDate; }
    public void setEndOfValidityDate(LocalDate endOfValidityDate) { this.endOfValidityDate = endOfValidityDate; }
}
