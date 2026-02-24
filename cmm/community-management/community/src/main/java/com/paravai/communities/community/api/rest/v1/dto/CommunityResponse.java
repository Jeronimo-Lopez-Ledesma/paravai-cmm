package com.paravai.communities.community.api.rest.v1.dto;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.value.CommunityRulesValue;
import com.paravai.communities.community.domain.value.ExchangeTypeValue;
import com.paravai.foundation.localization.MessageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityResponse {

    private String id;
    private String tenantId;

    private String name;
    private String slug;
    private String description;

    private String visibilityCode;
    private String visibilityLabel;

    private String statusCode;
    private String statusLabel;

    private Instant archivedAt;

    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    private CommunityRulesResponse rules;

    public static CommunityResponse fromDomain(
            Community c,
            Locale locale,
            MessageService messageService
    ) {

        CommunityRulesResponse rulesResponse = c.rules()
                .map(r -> CommunityRulesResponse.fromDomain(r, locale, messageService))
                .orElse(null);

        return CommunityResponse.builder()
                .id(c.id().value())
                .tenantId(c.tenantId().value())

                .name(c.name())
                .slug(c.slug())
                .description(c.description().orElse(null))

                .visibilityCode(c.visibility().getCode())
                .visibilityLabel(c.visibility().getLocalizedLabel(locale, messageService))

                .statusCode(c.status().getCode())
                .statusLabel(c.status().getLocalizedLabel(locale, messageService))

                .archivedAt(c.archivedAt().orElse(null))

                .createdBy(c.createdBy().value())
                .createdAt(c.createdAt())
                .updatedAt(c.updatedAt())

                .rules(rulesResponse)
                .build();
    }

    // -------------------------------------------------
    // Nested DTOs
    // -------------------------------------------------

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommunityRulesResponse {

        private String text;
        private List<ExchangeTypeResponse> allowedExchangeTypes;

        public static CommunityRulesResponse fromDomain(
                CommunityRulesValue rules,
                Locale locale,
                MessageService messageService
        ) {
            List<ExchangeTypeResponse> exchangeTypes =
                    rules.getAllowedExchangeTypes() != null
                            ? rules.getAllowedExchangeTypes().stream()
                            .map(v -> ExchangeTypeResponse.fromDomain(v, locale, messageService))
                            .toList()
                            : List.of();

            return CommunityRulesResponse.builder()
                    .text(rules.getText())
                    .allowedExchangeTypes(exchangeTypes)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExchangeTypeResponse {

        private String code;
        private String label;

        public static ExchangeTypeResponse fromDomain(
                ExchangeTypeValue v,
                Locale locale,
                MessageService messageService
        ) {
            return ExchangeTypeResponse.builder()
                    .code(v.getCode())
                    .label(v.getLocalizedLabel(locale, messageService))
                    .build();
        }
    }
}