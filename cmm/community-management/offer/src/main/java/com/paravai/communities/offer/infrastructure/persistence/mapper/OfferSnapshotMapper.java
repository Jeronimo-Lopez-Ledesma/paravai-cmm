package com.paravai.communities.offer.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.communities.contracts.event.offer.OfferEventPayloadV1;
import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.infrastructure.event.mapper.OfferToEventPayloadMapperV1;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes Offer aggregates into JSON snapshots for historization.
 */
@Component
public class OfferSnapshotMapper implements SnapshotMapper<Offer> {

    private final ObjectMapper objectMapper;
    private final OfferToEventPayloadMapperV1 payloadMapper;

    public OfferSnapshotMapper(
            ObjectMapper objectMapper,
            OfferToEventPayloadMapperV1 payloadMapper
    ) {
        this.objectMapper = objectMapper;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public JsonNode toSnapshot(Offer aggregate) {
        return objectMapper.valueToTree(payloadMapper.map(aggregate));
    }
}