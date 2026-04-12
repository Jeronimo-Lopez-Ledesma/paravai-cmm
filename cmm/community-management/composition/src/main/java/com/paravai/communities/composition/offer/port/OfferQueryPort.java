package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port used by composition/orchestration layer
 * to query Offer module.
 */
public interface OfferQueryPort {

    /**
     * Returns a paginated slice of offers owned by the provided owner.
     *
     * Expected behavior:
     * - only offers belonging to ownerId are returned
     * - if statusCode is null or blank, no status filter is applied
     * - if statusCode is invalid, the adapter should fail with a validation error
     */
    Flux<OfferSummary> listMine(
            String tenantId,
            String ownerId,
            String statusCode,
            int page,
            int size
    );

    /**
     * Returns the total number of offers owned by the provided owner,
     * using the same optional status filter as listMine(...).
     */
    Mono<Long> countMine(
            String tenantId,
            String ownerId,
            String statusCode
    );

    /**
     * Returns a paginated slice of ACTIVE offers visible inside the provided community.
     *
     * Expected behavior:
     * - only ACTIVE offers belonging to communityId are returned
     * - tenant isolation is preserved
     * - pagination is applied
     */
    Flux<OfferSummary> listCommunityOffers(
            String tenantId,
            String communityId,
            int page,
            int size
    );

    /**
     * Returns the total number of ACTIVE offers visible inside the provided community.
     */
    Mono<Long> countCommunityOffers(
            String tenantId,
            String communityId
    );
}