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

                membership.id().value(),
                membership.tenantId().value(),
                membership.communityId().value(),
                membership.userId().value(),

                membership.role().map(r -> r.getCode()).orElse(null),
                membership.role().map(r -> r.getLabel()).orElse(null),

                membership.status().getCode(),
                membership.status().getLabel(),

                membership.requestedAt().getInstant(),
                membership.decidedAt().map(TimestampValue::getInstant).orElse(null),

                membership.rejectionReason().orElse(null),

                membership.createdAt().getInstant(),
                membership.updatedAt().getInstant()
        );
    }
}