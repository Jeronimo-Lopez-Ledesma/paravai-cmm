package com.paravai.communities.offer.infrastructure.event.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.contracts.event.offer.OfferEventPayloadV1;
import com.paravai.foundation.domain.event.EntityChangedEvent;
import com.paravai.foundation.integration.domain.event.DomainEventEnvelope;
import com.paravai.foundation.integration.domain.event.EventChannel;
import com.paravai.foundation.integration.domain.event.EventCmm;
import com.paravai.foundation.integration.domain.event.SchemaId;
import com.paravai.foundation.integration.mapper.DomainEventEnvelopeFactory;
import org.springframework.stereotype.Component;

@Component
public class OfferEventEnvelopeMapper {

    private static final EventCmm CMM = EventCmm.COMMUNITIES_MANAGEMENT;
    private static final String COMPONENT = "offer";
    private static final int MAJOR = 1;

    private final OfferSnapshotToEventPayloadMapperV1 snapshotMapper;

    public OfferEventEnvelopeMapper(OfferSnapshotToEventPayloadMapperV1 snapshotMapper) {
        this.snapshotMapper = snapshotMapper;
    }

    public DomainEventEnvelope<OfferEventPayloadV1> map(EntityChangedEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("EntityChangedEvent must not be null");
        }

        JsonNode snapshot = event.getCurrentState() != null
                ? event.getCurrentState()
                : event.getPreviousState();

        if (snapshot == null || snapshot.isNull()) {
            throw new IllegalStateException("Cannot build OfferEventPayloadV1: snapshot is null");
        }

        OfferEventPayloadV1 payload = snapshotMapper.map(snapshot);

        String schemaId = SchemaId.of(CMM, COMPONENT, EventChannel.INTEGRATION, MAJOR);

        return DomainEventEnvelopeFactory.create(event, schemaId, payload);
    }
}