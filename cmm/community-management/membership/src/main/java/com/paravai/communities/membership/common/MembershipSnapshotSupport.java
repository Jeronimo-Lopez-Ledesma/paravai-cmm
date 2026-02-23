package com.paravai.communities.membership.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.foundation.snapshot.SnapshotMapper;

import java.util.Objects;

public final class MembershipSnapshotSupport {

    private final SnapshotMapper<Membership> snapshotMapper;

    public MembershipSnapshotSupport(SnapshotMapper<Membership> snapshotMapper) {
        this.snapshotMapper = Objects.requireNonNull(snapshotMapper, "snapshotMapper");
    }

    public JsonNode snapshot(Membership standard) {
        return snapshotMapper.toSnapshot(standard);
    }
}
