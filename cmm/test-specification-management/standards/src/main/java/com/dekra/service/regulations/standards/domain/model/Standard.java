package com.dekra.service.regulations.standards.domain.model;

import com.dekra.service.foundation.domain.organization.value.OrganizationAssociationValue;
import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.domaincore.value.TimestampValue;
import com.dekra.service.regulations.standards.domain.exception.DuplicateStandardVersionException;
import com.dekra.service.regulations.standards.domain.value.*;

import java.util.*;

public final class Standard {

    private final IdValue id;

    private final StandardCodeValue code;
    private final StandardTitleValue title;

    private final StandardTypeValue type; // mandatory (catalog VO)

    private final List<StandardVersion> versions;

    private final TimestampValue createdAt;
    private final TimestampValue updatedAt;

    private final String description; // optional

    /** Weak relationship: issuing standard body (Organization). Mandatory. */
    private final OrganizationAssociationValue standardizationBody; // mandatory

    // Package-private: only domain package (Factory) can call it.
    Standard(IdValue id,
             StandardCodeValue code,
             StandardTitleValue title,
             StandardTypeValue type,
             List<StandardVersion> versions,
             TimestampValue createdAt,
             TimestampValue updatedAt,
             String description,
             OrganizationAssociationValue standardizationBody) {

        this.id = Objects.requireNonNull(id, "id");
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.type = Objects.requireNonNull(type, "type"); // invariant: must be valid catalog value (VO enforces)
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");

        List<StandardVersion> copy = new ArrayList<>(Objects.requireNonNull(versions, "versions"));
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("Standard must have at least one version");
        }
        ensureNoDuplicateVersions(id, copy);

        this.versions = copy;
        this.description = description;
        this.standardizationBody = Objects.requireNonNull(standardizationBody, "issuingBody");
    }

    public Standard addVersion(StandardVersionValue version,
                               PublicationDateValue publicationDate,
                               VisibilityStatusValue visibility,
                               StandardVersionStatusValue status,
                               String versionDescription,
                               TimestampValue now) {

        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(visibility, "visibility");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(now, "now");

        StandardVersion candidate = StandardVersion.create(
                IdValue.generate(),
                version,
                publicationDate,
                visibility,
                status,
                versionDescription
        );

        boolean exists = versions.stream()
                .anyMatch(v -> v.uniquenessKey().equals(candidate.uniquenessKey()));

        if (exists) {
            throw new DuplicateStandardVersionException(this.id, version);
        }

        List<StandardVersion> newVersions = new ArrayList<>(this.versions);
        newVersions.add(candidate);

        return new Standard(
                this.id,
                this.code,
                this.title,
                this.type,
                newVersions,
                this.createdAt,
                now,
                this.description,
                this.standardizationBody
        );
    }

    /**
     * Updates "metadata" only (does not affect versions, issuing body, or type).
     */
    public Standard updateMetadata(StandardCodeValue code,
                                   StandardTitleValue title,
                                   String description,
                                   TimestampValue now) {

        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(now, "now");
        // description optional

        return new Standard(
                this.id,
                code,
                title,
                this.type,
                this.versions,
                this.createdAt,
                now,
                description,
                this.standardizationBody
        );
    }

    /**
     * Changes the issuing body association (metadata-level change but separated as a dedicated command).
     * Does not affect versions or type.
     */
    public Standard changeIssuingBody(OrganizationAssociationValue issuingBody,
                                      TimestampValue now) {

        Objects.requireNonNull(issuingBody, "issuingBody");
        Objects.requireNonNull(now, "now");

        return new Standard(
                this.id,
                this.code,
                this.title,
                this.type,
                this.versions,
                this.createdAt,
                now,
                this.description,
                issuingBody
        );
    }

    /**
     * Changes the Standard type (classification).
     * This is intentionally NOT part of updateMetadata(), because it has business impact.
     * Does not affect versions or issuing body.
     */
    public Standard changeType(StandardTypeValue type,
                               TimestampValue now) {

        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(now, "now");

        return new Standard(
                this.id,
                this.code,
                this.title,
                type,
                this.versions,
                this.createdAt,
                now,
                this.description,
                this.standardizationBody
        );
    }

    public Standard addApplicabilityContext(IdValue versionId,
                                            IdValue certificationSchemeId,
                                            com.dekra.service.foundation.domaincore.value.DateValue effectiveDate,
                                            com.dekra.service.foundation.domaincore.value.DateValue endOfValidityDate,
                                            TimestampValue now) {

        Objects.requireNonNull(versionId, "versionId");
        Objects.requireNonNull(certificationSchemeId, "certificationSchemeId");
        Objects.requireNonNull(effectiveDate, "effectiveDate");
        Objects.requireNonNull(now, "now");

        List<StandardVersion> newVersions = new ArrayList<>(this.versions);
        int idx = indexOfVersion(versionId, newVersions);

        StandardVersion updated = newVersions.get(idx)
                .addApplicabilityContext(certificationSchemeId, effectiveDate, endOfValidityDate);

        newVersions.set(idx, updated);

        return new Standard(
                this.id,
                this.code,
                this.title,
                this.type,
                newVersions,
                this.createdAt,
                now,
                this.description,
                this.standardizationBody
        );
    }

    public Standard removeApplicabilityContext(IdValue versionId,
                                               IdValue applicabilityContextId,
                                               TimestampValue now) {

        Objects.requireNonNull(versionId, "versionId");
        Objects.requireNonNull(applicabilityContextId, "applicabilityContextId");
        Objects.requireNonNull(now, "now");

        List<StandardVersion> newVersions = new ArrayList<>(this.versions);
        int idx = indexOfVersion(versionId, newVersions);

        StandardVersion updated = newVersions.get(idx)
                .removeApplicabilityContext(applicabilityContextId);

        newVersions.set(idx, updated);

        return new Standard(
                this.id,
                this.code,
                this.title,
                this.type,
                newVersions,
                this.createdAt,
                now,
                this.description,
                this.standardizationBody
        );
    }

    private static int indexOfVersion(IdValue versionId, List<StandardVersion> versions) {
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).id().equals(versionId)) return i;
        }
        throw new NoSuchElementException("StandardVersion not found: " + versionId);
    }



    // -------------------------
    // Getters
    // -------------------------

    public IdValue id() { return id; }
    public StandardCodeValue code() { return code; }
    public StandardTitleValue title() { return title; }
    public StandardTypeValue type() { return type; }

    public List<StandardVersion> versions() {
        return Collections.unmodifiableList(versions);
    }

    public TimestampValue createdAt() { return createdAt; }
    public TimestampValue updatedAt() { return updatedAt; }

    public String description() { return description; }

    public OrganizationAssociationValue issuingBody() { return standardizationBody; }

    // -------------------------
    // Invariants
    // -------------------------

    private static void ensureNoDuplicateVersions(IdValue standardId, List<StandardVersion> versions) {
        HashSet<String> seen = new HashSet<>();
        for (StandardVersion v : versions) {
            String key = v.uniquenessKey();
            if (!seen.add(key)) {
                throw new DuplicateStandardVersionException(standardId, v.version());
            }
        }
    }
}