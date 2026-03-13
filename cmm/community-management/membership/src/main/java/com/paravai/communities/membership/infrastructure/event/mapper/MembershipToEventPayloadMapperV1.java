package com.paravai.communities.membership.infrastructure.event.mapper;

import com.paravai.communities.contracts.event.membership.MembershipEventPayloadV1;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.foundation.domain.value.TimestampValue;
import org.springframework.stereotype.Component;

@Component
public class MembershipToEventPayloadMapperV1 {

    public MembershipEventPayloadV1 map(Membership membership) {
        if (membership == null) {
            throw new IllegalArgumentException("Membership must not be null");
        }

        return new MembershipEventPayloadV1(
                membership.id() != null ? membership.id().value() : null,
                membership.tenantId() != null ? membership.tenantId().value() : null,
                membership.communityId() != null ? membership.communityId().value() : null,
                membership.userId() != null ? membership.userId().value() : null,
                membership.role() != null ? membership.role().getCode() : null,
                membership.role() != null ? membership.role().getLabel() : null,
                membership.status() != null ? membership.status().getCode() : null,
                membership.status() != null ? membership.status().getLabel() : null,
                membership.since() != null ? membership.since().getInstant() : null,
                membership.deactivatedAt().map(TimestampValue::getInstant).orElse(null),
                membership.createdAt() != null ? membership.createdAt().getInstant() : null,
                membership.updatedAt() != null ? membership.updatedAt().getInstant() : null
        );
    }
}