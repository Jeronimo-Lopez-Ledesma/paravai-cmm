package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Mono;

/**
 * Outbound port used by composition/orchestration layer
 * to query Membership module.
 */
public interface MembershipQueryPort {

    /**
     * Returns membership information for a user in a community.
     *
     * Expected behavior:
     * - empty if no membership exists
     */
    Mono<MembershipSummary> findByUserInCommunity(
            String tenantId,
            String communityId,
            String userId
    );
}