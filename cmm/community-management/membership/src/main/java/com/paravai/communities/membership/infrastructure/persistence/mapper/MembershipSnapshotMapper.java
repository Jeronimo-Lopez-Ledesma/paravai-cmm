package com.paravai.communities.membership.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.infrastructure.event.MembershipEventPayloadV1;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes S aggregates into JSON snapshots for historization.
 */
@Component
public class MembershipSnapshotMapper implements SnapshotMapper<Membership> {

    private final ObjectMapper objectMapper;

    public MembershipSnapshotMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public JsonNode toSnapshot(Membership aggregate) {
        // Snapshot is a stable, serializable contract object (not the domain)
        return objectMapper.valueToTree(MembershipEventPayloadV1.from(aggregate));
    }
}
