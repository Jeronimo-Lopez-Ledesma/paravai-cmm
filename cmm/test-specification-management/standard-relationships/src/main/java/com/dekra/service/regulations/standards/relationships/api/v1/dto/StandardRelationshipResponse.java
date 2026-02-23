package com.dekra.service.regulations.standards.relationships.api.v1.dto;

import com.dekra.service.foundation.localization.MessageService;
import com.dekra.service.regulations.standards.relationships.domain.model.StandardRelationship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Locale;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardRelationshipResponse {

    private String id;

    // Weak references
    private String fromStandardId;
    private String fromVersionId;

    private String toStandardId;
    private String toVersionId;

    private String relationshipTypeCode;
    private String relationshipTypeLabel;

    private String relationshipPurposeCode;   // nullable
    private String relationshipPurposeLabel;  // nullable

    private Instant createdAt;

    public static StandardRelationshipResponse fromDomain(StandardRelationship r,
                                                          Locale locale,
                                                          MessageService messageService) {

        return StandardRelationshipResponse.builder()
                .id(r.id().getValue())

                .fromStandardId(r.from().standardId().getValue())
                .fromVersionId(r.from().versionId().getValue())

                .toStandardId(r.to().standardId().getValue())
                .toVersionId(r.to().versionId().getValue())

                .relationshipTypeCode(r.type().getCode())
                .relationshipTypeLabel(
                        r.type().getLocalizedLabel(locale, messageService)
                )

                .relationshipPurposeCode(
                        r.purpose() != null ? r.purpose().getCode() : null
                )
                .relationshipPurposeLabel(
                        r.purpose() != null
                                ? r.purpose().getLocalizedLabel(locale, messageService)
                                : null
                )

                .createdAt(r.createdAt().getInstant())
                .build();
    }
}
