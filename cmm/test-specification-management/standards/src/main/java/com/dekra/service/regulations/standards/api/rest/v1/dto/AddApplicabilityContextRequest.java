package com.paravai.regulations.standards.api.rest.v1.dto;

import com.paravai.foundation.domaincore.value.DateValue;
import com.paravai.foundation.domaincore.value.IdValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AddApplicabilityContextRequest {

    @NotBlank
    private String certificationSchemeId;

    @NotNull
    private LocalDate effectiveDate;

    private LocalDate endOfValidityDate; // optional

    // --- Mapping helpers (API -> Domain VOs) ---
    public IdValue toCertificationSchemeId() { return IdValue.of(certificationSchemeId); }
    public DateValue toEffectiveDateVo() { return DateValue.of(effectiveDate); }
    public DateValue toEndOfValidityDateVoOrNull() {
        return endOfValidityDate != null ? DateValue.of(endOfValidityDate) : null;
    }

    // getters/setters
    public String getCertificationSchemeId() { return certificationSchemeId; }
    public void setCertificationSchemeId(String certificationSchemeId) { this.certificationSchemeId = certificationSchemeId; }

    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }

    public LocalDate getEndOfValidityDate() { return endOfValidityDate; }
    public void setEndOfValidityDate(LocalDate endOfValidityDate) { this.endOfValidityDate = endOfValidityDate; }
}
