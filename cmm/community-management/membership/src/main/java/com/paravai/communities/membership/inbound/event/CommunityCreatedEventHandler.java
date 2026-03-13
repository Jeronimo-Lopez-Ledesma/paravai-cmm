package com.paravai.communities.membership.inbound.event.handler;

import com.paravai.communities.contracts.event.community.CommunityEventPayloadV1;
import com.paravai.communities.membership.application.command.createfounder.CreateFounderMembershipService;
import com.paravai.communities.membership.inbound.event.mapper.CommunityEventToCreateFounderMembershipRequestMapper;
import com.paravai.foundation.integration.application.inbound.dispatcher.EventInboundHandler;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CommunityCreatedEventHandler implements EventInboundHandler {

    private static final String EXPECTED_ENTITY_TYPE = "Community";
    private static final String EXPECTED_CHANGE_TYPE = "CREATED";
    private static final String EXPECTED_SCHEMA_ID = "communities.community.integration.v1";

    private final CommunityEventToCreateFounderMembershipRequestMapper mapper;
    private final CreateFounderMembershipService service;

    public CommunityCreatedEventHandler(
            CommunityEventToCreateFounderMembershipRequestMapper mapper,
            CreateFounderMembershipService service
    ) {
        this.mapper = mapper;
        this.service = service;
    }

    @Override
    public boolean supports(DomainEventEnvelope<?> event) {
        if (event == null) {
            return false;
        }

        if (!(event.getPayload() instanceof CommunityEventPayloadV1)) {
            return false;
        }

        return EXPECTED_SCHEMA_ID.equals(event.getSchemaId())
                && EXPECTED_ENTITY_TYPE.equals(event.getEntityType())
                && EXPECTED_CHANGE_TYPE.equalsIgnoreCase(event.getChangeType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> handle(DomainEventEnvelope<?> event) {
        if (event == null) {
            return Mono.empty();
        }

        DomainEventEnvelope<CommunityEventPayloadV1> typedEvent =
                (DomainEventEnvelope<CommunityEventPayloadV1>) event;

        return service.createFounderMembership(mapper.map(typedEvent));
    }
}