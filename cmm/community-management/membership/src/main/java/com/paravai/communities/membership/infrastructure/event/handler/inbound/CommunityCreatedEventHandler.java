package com.paravai.communities.membership.infrastructure.event.handler.inbound;

import com.paravai.communities.community.infrastructure.event.CommunityEventPayloadV1;
import com.paravai.communities.membership.application.command.create.CreateInitialMembershipOnCommunityCreatedService;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.domain.value.IdValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CommunityCreatedEventHandler {

    private static final Logger log =
            LoggerFactory.getLogger(CommunityCreatedEventHandler.class);

    private final CreateInitialMembershipOnCommunityCreatedService service;

    public CommunityCreatedEventHandler(
            CreateInitialMembershipOnCommunityCreatedService service
    ) {
        this.service = service;
    }

    public Mono<Void> handle(DomainEventEnvelope<CommunityEventPayloadV1> envelope) {

        CommunityEventPayloadV1 payload = envelope.getPayload();

        IdValue tenantId = IdValue.of(payload.getTenantId());
        IdValue communityId = IdValue.of(payload.getId());
        IdValue creatorUserId = IdValue.of(payload.getCreatedBy());

        log.info(
                "Processing CommunityCreated event for community {}",
                communityId
        );

        return service.createAdminMembership(
                tenantId,
                communityId,
                creatorUserId,
                envelope.getTraceId(),
                envelope.getSourceService()
        );
    }
}