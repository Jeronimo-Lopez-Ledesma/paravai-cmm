package com.paravai.regulations.standards.api.rest.v1.dto;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO to change the type of a Standard.
 */
public final class ChangeStandardTypeRequest {

    @NotBlank(message = "standardTypeCode must not be blank")
    private String standardTypeCode;

    public ChangeStandardTypeRequest() {
        // Required by Jackson
    }

    public ChangeStandardTypeRequest(String standardTypeCode) {
        this.standardTypeCode = standardTypeCode;
    }

    public String getStandardTypeCode() {
        return standardTypeCode;
    }

    public void setStandardTypeCode(String standardTypeCode) {
        this.standardTypeCode = standardTypeCode;
    }
}