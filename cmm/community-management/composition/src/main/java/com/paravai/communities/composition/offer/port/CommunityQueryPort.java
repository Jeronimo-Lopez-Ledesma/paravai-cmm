package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Mono;

/**
 * Outbound port used by composition/orchestration layer
 * to query Community module.
 */
public interface CommunityQueryPort {

    /**
     * Returns community policy required for offer publishing.
     *
     * Expected behavior:
     * - empty if community does not exist
     */
    Mono<CommunitySummary> findOfferPolicy(String communityId);
}