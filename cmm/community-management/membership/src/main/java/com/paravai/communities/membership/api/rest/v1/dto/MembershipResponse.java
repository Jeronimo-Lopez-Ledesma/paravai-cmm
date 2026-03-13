package com.paravai.communities.membership.api.rest.v1.dto;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.foundation.domain.value.TimestampValue;
import com.paravai.foundation.localization.MessageService;
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
public class MembershipResponse {

    private String id;

    private String tenantId;
    private String communityId;
    private String userId;

    private String roleCode;
    private String roleLabel;

    private String statusCode;
    private String statusLabel;

    private Instant since;
    private Instant deactivatedAt;

    private Instant createdAt;
    private Instant updatedAt;

    public static MembershipResponse fromDomain(
            Membership m,
            Locale locale,
            MessageService messageService
    ) {
        return MembershipResponse.builder()
                .id(m.id().value())

                .tenantId(m.tenantId().value())
                .communityId(m.communityId().value())
                .userId(m.userId().value())

                .roleCode(m.role().getCode())
                .roleLabel(m.role().getLocalizedLabel(locale, messageService))

                .statusCode(m.status().getCode())
                .statusLabel(m.status().getLocalizedLabel(locale, messageService))

                .since(m.since().getInstant())
                .deactivatedAt(m.deactivatedAt().map(TimestampValue::getInstant).orElse(null))

                .createdAt(m.createdAt().getInstant())
                .updatedAt(m.updatedAt().getInstant())
                .build();
    }
}