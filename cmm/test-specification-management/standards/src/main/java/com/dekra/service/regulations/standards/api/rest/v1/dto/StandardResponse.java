package com.paravai.regulations.standards.api.rest.v1.dto;

import com.paravai.foundation.localization.MessageService;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.model.StandardVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardResponse {

    private String id;

    private String code;
    private String codeKey;

    private String title;
    private String description;

    private String standardTypeCode;   // NEW
    private String standardTypeLabel;  // NEW

    private String standardizationBodyId;

    private Instant createdAt;
    private Instant updatedAt;

    private List<StandardVersionResponse> versions;

    public static StandardResponse fromDomain(Standard s, Locale locale, MessageService messageService) {

        List<StandardVersionResponse> versionDtos = s.versions() != null
                ? s.versions().stream()
                .map(v -> StandardVersionResponse.fromDomain(v, locale, messageService))
                .toList()
                : List.of();

        return StandardResponse.builder()
                .id(s.id().getValue())
                .code(s.code().value())
                .codeKey(s.code().normalizedKey())
                .title(s.title().value())
                .description(s.description())
                .standardTypeCode(s.type().getCode())
                .standardTypeLabel(s.type().getLocalizedLabel(locale,messageService))
                .standardizationBodyId(
                        s.issuingBody().getOrganizationId().getValue()
                )
                .createdAt(s.createdAt().getInstant())
                .updatedAt(s.updatedAt().getInstant())
                .versions(versionDtos)
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StandardVersionResponse {

        private String id;
        private String version;
        private String versionKey;

        private String visibility;

        private String statusCode;
        private String statusLabel;

        private LocalDate publicationDate;
        private String description;

        private List<ApplicabilityContextResponse> applicabilityContexts;


        public static StandardVersionResponse fromDomain(StandardVersion v,Locale locale, MessageService messageService) {
            List<ApplicabilityContextResponse> contexts = v.applicabilityContexts() != null
                    ? v.applicabilityContexts().stream()
                    .map(ApplicabilityContextResponse::fromDomain)
                    .toList()
                    : List.of();

            return StandardVersionResponse.builder()
                    .id(v.id().getValue())
                    .version(v.version().value())
                    .versionKey(v.version().normalizedKey())
                    .visibility(v.visibility().value())

                    .statusCode(v.status().getCode())
                    .statusLabel(v.status().getLocalizedLabel(locale,messageService))

                    .publicationDate(v.publicationDate() != null ? v.publicationDate().value() : null)
                    .description(v.description())

                    .applicabilityContexts(contexts)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicabilityContextResponse {

        private String id;
        private String certificationSchemeId;

        private LocalDate effectiveDate;
        private LocalDate endOfValidityDate;

        private Instant createdAt;

        public static ApplicabilityContextResponse fromDomain(
                com.paravai.regulations.standards.domain.model.ApplicabilityContext c
        ) {
            return ApplicabilityContextResponse.builder()
                    .id(c.id().getValue())
                    .certificationSchemeId(c.certificationSchemeId().getValue())
                    .effectiveDate(c.effectiveDate().getDate())
                    .endOfValidityDate(c.endOfValidityDate() != null ? c.endOfValidityDate().getDate() : null)
                    .createdAt(c.createdAt().getInstant())
                    .build();
        }
    }

}