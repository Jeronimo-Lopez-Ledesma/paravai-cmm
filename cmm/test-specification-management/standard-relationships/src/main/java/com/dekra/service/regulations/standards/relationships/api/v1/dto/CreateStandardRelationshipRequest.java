package com.paravai.regulations.standards.relationships.api.v1.dto;

import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;
import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;
import com.paravai.regulations.standards.relationships.domain.value.StandardVersionRefValue;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO to create a StandardRelationship between two Standard Versions.
 *
 * Weak references:
 * - fromStandardId + fromVersionId
 * - toStandardId + toVersionId
 *
 * typeCode: catalog code (e.g. REPLACES, REFERS_TO, AMENDS, DERIVED_FROM, EQUIVALENT_TO)
 * purposeCode: only allowed for REFERS_TO (NORMATIVE / INFORMATIVE), otherwise must be absent.
 */
public final class CreateStandardRelationshipRequest {

    @NotBlank(message = "fromStandardId must not be blank")
    private String fromStandardId;

    @NotBlank(message = "fromVersionId must not be blank")
    private String fromVersionId;

    @NotBlank(message = "toStandardId must not be blank")
    private String toStandardId;

    @NotBlank(message = "toVersionId must not be blank")
    private String toVersionId;

    @NotBlank(message = "typeCode must not be blank")
    private String typeCode;

    /**
     * Optional. Only for REFERS_TO relationships (NORMATIVE / INFORMATIVE).
     */
    private String purposeCode;

    public CreateStandardRelationshipRequest() {
        // Required by Jackson
    }

    // -------------------------
    // Mapping helpers
    // -------------------------

    public StandardVersionRefValue toFromRefVo() {
        return StandardVersionRefValue.of(
                IdValue.of(fromStandardId),
                IdValue.of(fromVersionId)
        );
    }

    public StandardVersionRefValue toToRefVo() {
        return StandardVersionRefValue.of(
                IdValue.of(toStandardId),
                IdValue.of(toVersionId)
        );
    }

    public StandardRelationshipTypeValue toTypeVo() {
        return StandardRelationshipTypeValue.of(typeCode);
    }

    public StandardRelationshipPurposeValue toPurposeVoOrNull() {
        return (purposeCode == null || purposeCode.isBlank())
                ? null
                : StandardRelationshipPurposeValue.of(purposeCode);
    }

    // -------------------------
    // Getters/Setters
    // -------------------------

    public String getFromStandardId() { return fromStandardId; }
    public void setFromStandardId(String fromStandardId) { this.fromStandardId = fromStandardId; }

    public String getFromVersionId() { return fromVersionId; }
    public void setFromVersionId(String fromVersionId) { this.fromVersionId = fromVersionId; }

    public String getToStandardId() { return toStandardId; }
    public void setToStandardId(String toStandardId) { this.toStandardId = toStandardId; }

    public String getToVersionId() { return toVersionId; }
    public void setToVersionId(String toVersionId) { this.toVersionId = toVersionId; }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getPurposeCode() { return purposeCode; }
    public void setPurposeCode(String purposeCode) { this.purposeCode = purposeCode; }
}
