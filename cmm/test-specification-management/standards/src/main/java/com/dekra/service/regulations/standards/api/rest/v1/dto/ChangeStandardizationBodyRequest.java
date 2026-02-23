package com.paravai.regulations.standards.api.rest.v1.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO to change the issuing body of a Standard.
  */
public final class ChangeStandardizationBodyRequest {

    @NotBlank(message = "issuingBodyOrganizationId must not be blank")
    private String standardizationBodyId;

    public ChangeStandardizationBodyRequest() {
        // Required by Jackson
    }

    public ChangeStandardizationBodyRequest(String standardizationBodyId) {
        this.standardizationBodyId = standardizationBodyId;
    }

    public String getStandardizationBodyId() {
        return standardizationBodyId;
    }

    public void setStandardizationBodyId(String standardizationBodyId) {
        this.standardizationBodyId = standardizationBodyId;
    }
}