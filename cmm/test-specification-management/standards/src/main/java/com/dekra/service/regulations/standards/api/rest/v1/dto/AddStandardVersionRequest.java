package com.paravai.regulations.standards.api.rest.v1.dto;

import com.paravai.regulations.standards.domain.value.PublicationDateValue;
import com.paravai.regulations.standards.domain.value.StandardVersionStatusValue;
import com.paravai.regulations.standards.domain.value.StandardVersionValue;
import com.paravai.regulations.standards.domain.value.VisibilityStatusValue;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class AddStandardVersionRequest {

    @NotBlank
    private String version;

    @NotBlank
    private String visibility; // PUBLIC / INTERNAL

    @NotBlank
    private String status;     // DRAFT / PUBLISHED / SUPERSEDED / WITHDRAWN

    private LocalDate publicationDate; // optional
    private String description;        // optional

    // --- Mapping helpers (API -> Domain VOs) ---

    public StandardVersionValue toVersionVo() {
        return StandardVersionValue.of(version);
    }

    public VisibilityStatusValue toVisibilityVo() {
        return VisibilityStatusValue.of(visibility);
    }

    public StandardVersionStatusValue toStatusVo() {
        return StandardVersionStatusValue.of(status);
    }

    public PublicationDateValue toPublicationDateVoOrNull() {
        return publicationDate != null ? PublicationDateValue.of(publicationDate) : null;
    }

    public String toVersionDescriptionOrNull() {
        return description;
    }

    // getters/setters
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