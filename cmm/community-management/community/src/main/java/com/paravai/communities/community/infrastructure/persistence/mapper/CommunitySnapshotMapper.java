package com.paravai.communities.community.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.infrastructure.event.mapper.CommunityToEventPayloadMapperV1;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes Community aggregates into JSON snapshots for historization.
 */
@Component
public class CommunitySnapshotMapper implements SnapshotMapper<Community> {

    private final ObjectMapper objectMapper;
    private final CommunityToEventPayloadMapperV1 payloadMapper;

    public CommunitySnapshotMapper(
            ObjectMapper objectMapper,
            CommunityToEventPayloadMapperV1 payloadMapper
    ) {
        this.objectMapper = objectMapper;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public JsonNode toSnapshot(Community aggregate) {
        return objectMapper.valueToTree(payloadMapper.map(aggregate));
    }
}