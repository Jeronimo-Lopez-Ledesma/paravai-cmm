package com.dekra.service.regulations.standards.api.rest.v1.dto;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.regulations.standards.domain.model.StandardVersion;
import com.dekra.service.regulations.standards.domain.value.*;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class InitialVersionRequest {

    @NotBlank
    private String version;

    @NotBlank
    private String visibility; // PUBLIC / INTERNAL

    @NotBlank
    private String status;     // DRAFT / PUBLISHED / SUPERSEDED / WITHDRAWN

    private LocalDate publicationDate; // optional
    private String description;        // optional

    public StandardVersion toDomain() {
        return StandardVersion.create(
                IdValue.generate(),
                StandardVersionValue.of(version),
                publicationDate != null ? PublicationDateValue.of(publicationDate) : null,
                VisibilityStatusValue.of(visibility),
                StandardVersionStatusValue.of(status),
                description
        );
    }

    // -------------------------
    // Getters/Setters
    // -------------------------

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}