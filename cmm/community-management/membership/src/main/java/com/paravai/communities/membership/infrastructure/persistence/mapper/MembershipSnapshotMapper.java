package com.paravai.communities.membership.infrastructure.persistence.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paravai.communities.contracts.event.membership.MembershipEventPayloadV1;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.infrastructure.event.mapper.MembershipToEventPayloadMapperV1;
import com.paravai.foundation.snapshot.SnapshotMapper;
import org.springframework.stereotype.Component;

/**
 * Serializes S aggregates into JSON snapshots for historization.
 */
@Component
public class MembershipSnapshotMapper implements SnapshotMapper<Membership> {

    private final ObjectMapper objectMapper;
    private final MembershipToEventPayloadMapperV1 payloadMapper;

    public MembershipSnapshotMapper(
            ObjectMapper objectMapper,
            MembershipToEventPayloadMapperV1 payloadMapper
    ) {
        this.objectMapper = objectMapper;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public JsonNode toSnapshot(Membership aggregate) {
        return objectMapper.valueToTree(payloadMapper.map(aggregate));
    }
}
