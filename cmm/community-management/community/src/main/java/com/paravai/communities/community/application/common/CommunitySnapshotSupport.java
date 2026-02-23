package com.paravai.communities.community.application.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.foundation.snapshot.SnapshotMapper;


import java.util.Objects;

public final class CommunitySnapshotSupport {

    private final SnapshotMapper<Community> snapshotMapper;

    public CommunitySnapshotSupport(SnapshotMapper<Community> snapshotMapper) {
        this.snapshotMapper = Objects.requireNonNull(snapshotMapper, "snapshotMapper");
    }

    public JsonNode snapshot(Community standard) {
        return snapshotMapper.toSnapshot(standard);
    }
}
