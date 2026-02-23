package com.dekra.service.regulations.standards.domain.model;

import com.dekra.service.foundation.domaincore.value.DateValue;
import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.regulations.standards.domain.value.PublicationDateValue;
import com.dekra.service.regulations.standards.domain.value.StandardVersionStatusValue;
import com.dekra.service.regulations.standards.domain.value.StandardVersionValue;
import com.dekra.service.regulations.standards.domain.value.VisibilityStatusValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entity inside the Standard aggregate.
 *
 * Identity is local to the aggregate (IdValue).
 * Uniqueness is enforced by the aggregate using version.normalizedKey().
 *
 * Represents a concrete published (or draft/superseded/withdrawn) version
 * of a Standard.
 */
public final class StandardVersion {

    private final IdValue id;
    private final StandardVersionValue version;

    private final PublicationDateValue publicationDate; // optional
    private final VisibilityStatusValue visibility;      // required
    private final StandardVersionStatusValue status;     // required

    private final String description;                    // optional

    private final List<ApplicabilityContext> applicabilityContexts; // NEW

    private StandardVersion(IdValue id,
                            StandardVersionValue version,
                            PublicationDateValue publicationDate,
                            VisibilityStatusValue visibility,
                            StandardVersionStatusValue status,
                            String description,
                            List<ApplicabilityContext> applicabilityContexts) {

        this.id = Objects.requireNonNull(id, "id");
        this.version = Objects.requireNonNull(version, "version");
        this.visibility = Objects.requireNonNull(visibility, "visibility");
        this.status = Objects.requireNonNull(status, "status");

        this.publicationDate = publicationDate;
        this.description = description;

        this.applicabilityContexts = new ArrayList<>(Objects.requireNonNull(applicabilityContexts, "applicabilityContexts"));

        validateInvariants();
    }

    public static StandardVersion create(IdValue id,
                                         StandardVersionValue version,
                                         PublicationDateValue publicationDate,
                                         VisibilityStatusValue visibility,
                                         StandardVersionStatusValue status,
                                         String description) {

        return new StandardVersion(
                id, version, publicationDate, visibility, status, description,
                List.of() // NEW: start empty
        );
    }

    public static StandardVersion recreate(IdValue id,
                                           StandardVersionValue version,
                                           PublicationDateValue publicationDate,
                                           VisibilityStatusValue visibility,
                                           StandardVersionStatusValue status,
                                           String description,
                                           List<ApplicabilityContext> applicabilityContexts) {

        return new StandardVersion(id, version, publicationDate, visibility, status, description, applicabilityContexts);
    }

    private void validateInvariants() {
        if (status.getCode().equals("DRAFT") && publicationDate != null) {
            throw new IllegalStateException("Draft versions must not have a publication date");
        }
        // If you later enforce "published must have publicationDate", do it here too.
    }

    // -------------------------
    // Applicability behavior (immutable "with" methods)
    // -------------------------

    public StandardVersion addApplicabilityContext(IdValue certificationSchemeId,
                                                   DateValue effectiveDate,
                                                   DateValue endOfValidityDate) {

        Objects.requireNonNull(certificationSchemeId, "certificationSchemeId");
        Objects.requireNonNull(effectiveDate, "effectiveDate");
        // endOfValidityDate optional

        // INV-12: effectiveDate must not be earlier than publication date (if publicationDate exists)
        if (publicationDate != null && effectiveDate.getDate().isBefore(publicationDate.value())) {
            throw new IllegalStateException("Effective date must not be earlier than publication date");
        }

        ApplicabilityContext candidate = ApplicabilityContext.create(
                IdValue.generate(),
                certificationSchemeId,
                effectiveDate,
                endOfValidityDate,
                TimestampValue.now()
        );

        // Recommended derived invariants:
        // - No overlap for same scheme
        // - At most one open-ended per scheme (covered by overlap + strict end>effective, but keep explicit)
        for (ApplicabilityContext existing : applicabilityContexts) {
            if (existing.certificationSchemeId().equals(certificationSchemeId)) {
                if (existing.overlaps(candidate)) {
                    throw new IllegalStateException("Applicability context overlaps with an existing one for the same certification scheme");
                }
            }
        }

        List<ApplicabilityContext> newList = new ArrayList<>(this.applicabilityContexts);
        newList.add(candidate);

        return new StandardVersion(
                this.id,
                this.version,
                this.publicationDate,
                this.visibility,
                this.status,
                this.description,
                newList
        );
    }

    public StandardVersion removeApplicabilityContext(IdValue applicabilityContextId) {
        Objects.requireNonNull(applicabilityContextId, "applicabilityContextId");

        boolean exists = applicabilityContexts.stream().anyMatch(c -> c.id().equals(applicabilityContextId));
        if (!exists) {
            return this; // idempotent delete (good for REST DELETE)
        }

        List<ApplicabilityContext> newList = applicabilityContexts.stream()
                .filter(c -> !c.id().equals(applicabilityContextId))
                .toList();

        return new StandardVersion(
                this.id,
                this.version,
                this.publicationDate,
                this.visibility,
                this.status,
                this.description,
                newList
        );
    }

    // -------------------------
    // Getters
    // -------------------------

    public IdValue id() { return id; }
    public StandardVersionValue version() { return version; }
    public PublicationDateValue publicationDate() { return publicationDate; }
    public VisibilityStatusValue visibility() { return visibility; }
    public StandardVersionStatusValue status() { return status; }
    public String description() { return description; }

    public List<ApplicabilityContext> applicabilityContexts() {
        return Collections.unmodifiableList(applicabilityContexts);
    }

    public String uniquenessKey() { return version.normalizedKey(); }
}