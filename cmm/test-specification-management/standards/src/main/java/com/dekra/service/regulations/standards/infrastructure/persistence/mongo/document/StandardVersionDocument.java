package com.paravai.regulations.standards.infrastructure.persistence.mongo.document;

import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.regulations.standards.domain.model.StandardVersion;
import com.paravai.regulations.standards.domain.value.PublicationDateValue;
import com.paravai.regulations.standards.domain.value.StandardVersionStatusValue;
import com.paravai.regulations.standards.domain.value.StandardVersionValue;
import com.paravai.regulations.standards.domain.value.VisibilityStatusValue;

import java.time.LocalDate;
import java.util.List;

public class StandardVersionDocument {

    private String id;

    private String version;
    private String versionKey;

    private String visibility;  // PUBLIC / INTERNAL (canonical)
    private String status;      // NEW (DRAFT / PUBLISHED / SUPERSEDED / WITHDRAWN)

    private LocalDate publicationDate; // optional
    private String description;        // optional

    private List<ApplicabilityContextDocument> applicabilityContexts;


    // -------------------------
    // Mapping
    // -------------------------

    public static StandardVersionDocument fromDomain(StandardVersion v) {
        StandardVersionDocument d = new StandardVersionDocument();
        d.id = v.id().getValue();

        d.version = v.version().value();
        d.versionKey = v.version().normalizedKey();

        d.visibility = v.visibility().value();
        d.status = v.status().getCode();   // NEW

        d.publicationDate = v.publicationDate() != null ? v.publicationDate().value() : null;
        d.description = v.description();

        d.applicabilityContexts = v.applicabilityContexts() != null
                ? v.applicabilityContexts().stream()
                .map(ApplicabilityContextDocument::fromDomain)
                .toList()
                : List.of();


        return d;
    }

    public StandardVersion toDomain() {

        if (status == null || status.isBlank()) {
            throw new IllegalStateException("Invalid StandardVersion document: status is required");
        }

        List varContexts = applicabilityContexts != null
                ? applicabilityContexts.stream().map(ApplicabilityContextDocument::toDomain).toList()
                : List.of();

        return StandardVersion.recreate(
                IdValue.of(id),
                StandardVersionValue.of(version),
                publicationDate != null ? PublicationDateValue.of(publicationDate) : null,
                VisibilityStatusValue.of(visibility),
                StandardVersionStatusValue.of(status),
                description,
                varContexts
        );

    }

    // -------------------------
    // Getters/Setters
    // -------------------------

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getVersionKey() { return versionKey; }
    public void setVersionKey(String versionKey) { this.versionKey = versionKey; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ApplicabilityContextDocument> getApplicabilityContexts() { return applicabilityContexts; }
    public void setApplicabilityContexts(List<ApplicabilityContextDocument> applicabilityContexts) { this.applicabilityContexts = applicabilityContexts; }
}