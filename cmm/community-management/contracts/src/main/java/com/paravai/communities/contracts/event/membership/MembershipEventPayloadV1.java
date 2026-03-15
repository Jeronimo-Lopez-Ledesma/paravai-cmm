package com.paravai.communities.contracts.event.membership;

import java.time.Instant;

public record MembershipEventPayloadV1(
        String membershipId,
        String tenantId,
        String communityId,
        String userId,
        String roleCode,
        String roleLabel,
        String statusCode,
        String statusLabel,
        Instant requestedAt,
        Instant decidedAt,
        String rejectionReason,
        Instant createdAt,
        Instant updatedAt
) {}