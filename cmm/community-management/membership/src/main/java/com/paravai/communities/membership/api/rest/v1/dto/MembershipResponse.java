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

    private Instant requestedAt;
    private Instant decidedAt;
    private String rejectionReason;

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

                .roleCode(m.role().map(r -> r.getCode()).orElse(null))
                .roleLabel(m.role().map(r -> r.getLocalizedLabel(locale, messageService)).orElse(null))

                .statusCode(m.status().getCode())
                .statusLabel(m.status().getLocalizedLabel(locale, messageService))

                .requestedAt(m.requestedAt().getInstant())
                .decidedAt(m.decidedAt().map(TimestampValue::getInstant).orElse(null))
                .rejectionReason(m.rejectionReason().orElse(null))

                .createdAt(m.createdAt().getInstant())
                .updatedAt(m.updatedAt().getInstant())
                .build();
    }
}
