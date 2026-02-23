package com.paravai.regulations.standards.api.grpc;

import com.paravai.foundation.domaincore.value.DateValue;
import com.paravai.regulations.standards.api.grpc.v1.ApplicabilityContext;
import com.paravai.regulations.standards.api.grpc.v1.Standard;
import com.paravai.regulations.standards.api.grpc.v1.StandardVersion;

public final class StandardGrpcMapper {

    private StandardGrpcMapper() {}

    public static Standard toProto(com.paravai.regulations.standards.domain.model.Standard s) {

        Standard.Builder b = Standard.newBuilder()
                .setId(s.id().getValue())
                .setCode(s.code().value())
                .setCodeKey(s.code().normalizedKey())
                .setTitle(s.title().value())
                .setStandardTypeCode(s.type().getCode())
                .setIssuingBodyOrganizationId(s.issuingBody().getOrganizationId().getValue())
                .setCreatedAt(s.createdAt().getInstant().toString())
                .setUpdatedAt(s.updatedAt().getInstant().toString());

        if (s.description() != null) b.setDescription(s.description());

        for (var v : s.versions()) {
            b.addVersions(toProto(v));
        }

        return b.build();
    }

    private static StandardVersion toProto(com.paravai.regulations.standards.domain.model.StandardVersion v) {

        StandardVersion.Builder b = StandardVersion.newBuilder()
                .setId(v.id().getValue())
                .setVersion(v.version().value())
                .setVersionKey(v.version().normalizedKey())
                .setVisibility(v.visibility().value())
                .setStatusCode(v.status().getCode());

        if (v.publicationDate() != null) {
            b.setPublicationDate(v.publicationDate().value().toString());
        }
        if (v.description() != null) {
            b.setDescription(v.description());
        }

        for (var ctx : v.applicabilityContexts()) {
            b.addApplicabilityContexts(toProto(ctx));
        }

        return b.build();
    }

    private static ApplicabilityContext toProto(com.paravai.regulations.standards.domain.model.ApplicabilityContext c) {

        ApplicabilityContext.Builder b = ApplicabilityContext.newBuilder()
                .setId(c.id().getValue())
                .setCertificationSchemeId(c.certificationSchemeId().getValue())
                .setEffectiveDate(c.effectiveDate().toIsoString()) // DateValue
                .setCreatedAt(c.createdAt().getInstant().toString());

        if (c.endOfValidityDate() != null) {
            b.setEndOfValidityDate(c.endOfValidityDate().toIsoString());
        }

        return b.build();
    }
}