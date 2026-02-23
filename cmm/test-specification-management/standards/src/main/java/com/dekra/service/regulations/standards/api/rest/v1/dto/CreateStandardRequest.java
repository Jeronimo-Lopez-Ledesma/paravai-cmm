package com.paravai.regulations.standards.api.rest.v1.dto;

import com.paravai.foundation.domain.organization.value.OrganizationAssociationValue;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.domaincore.value.TimestampValue;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.model.StandardFactory;
import com.paravai.regulations.standards.domain.model.StandardVersion;
import com.paravai.regulations.standards.domain.value.StandardCodeValue;
import com.paravai.regulations.standards.domain.value.StandardTitleValue;
import com.paravai.regulations.standards.domain.value.StandardTypeValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateStandardRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String title;

    /**
     * Catalog code for the Standard type (e.g. TECHNICAL_STANDARD, REGULATION).
     */
    @NotBlank
    private String standardTypeCode;

    private String description;

    @Valid
    @NotNull
    private InitialVersionRequest initialVersion;

    @NotBlank
    private String standardizationBodyId;

    public Standard toDomain() {
        final TimestampValue now = TimestampValue.now();

        StandardVersion v = initialVersion.toDomain();

        return StandardFactory.create(
                StandardCodeValue.of(code),
                StandardTitleValue.of(title),
                StandardTypeValue.of(standardTypeCode),
                v,
                description,
                OrganizationAssociationValue.forStandardIssuer(IdValue.of(standardizationBodyId)),
                now
        );
    }
}