package com.paravai.communities.community.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.infrastructure.event.CommunityEventPayloadV1;
import com.paravai.foundation.snapshot.SnapshotMapper;

import org.springframework.stereotype.Component;

/**
 * Serializes S aggregates into JSON snapshots for historization.
 */
@Component
public class CommunitySnapshotMapper implements SnapshotMapper<Community> {

    private final ObjectMapper objectMapper;

    public CommunitySnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode toSnapshot(Community aggregate) {
        // Snapshot is a stable, serializable contract object (not the domain)
        return objectMapper.valueToTree(CommunityEventPayloadV1.from(aggregate));
    }
}
