package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Mono;

/**
 * Outbound port used by composition/orchestration layer
 * to query Resource module.
 */
public interface ResourceQueryPort {

    /**
     * Returns the resource only if it belongs to the provided owner.
     *
     * Expected behavior:
     * - if resource does not exist, empty
     * - if resource exists but does not belong to owner, empty
     */
    Mono<ResourceSummary> findOwnedById(String resourceId, String ownerId);
}