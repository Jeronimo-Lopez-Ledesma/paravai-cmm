package com.paravai.communities.membership.inbound.event.mapper;

import com.paravai.communities.contracts.event.community.CommunityEventPayloadV1;
import com.paravai.communities.membership.application.command.createfounder.CreateFounderMembershipRequest;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import org.springframework.stereotype.Component;

@Component
public class CommunityEventToCreateFounderMembershipRequestMapper {

    public CreateFounderMembershipRequest map(DomainEventEnvelope<CommunityEventPayloadV1> event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null");
        }

        CommunityEventPayloadV1 payload = event.getPayload();
        if (payload == null) {
            throw new IllegalArgumentException("event payload must not be null");
        }

        IdValue tenantId = IdValue.of(payload.tenantId());
        IdValue communityId = IdValue.of(payload.communityId());
        IdValue founderUserId = IdValue.of(payload.createdBy());

        return new CreateFounderMembershipRequest(
                tenantId,
                communityId,
                founderUserId,
                event.getTraceId(),
                event.getSourceService()
        );
    }
}