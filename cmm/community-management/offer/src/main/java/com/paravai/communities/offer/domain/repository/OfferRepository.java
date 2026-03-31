package com.paravai.communities.offer.domain.repository;

import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain port for Offer aggregate.
 *
 * Hexagonal Architecture:
 * - no Spring
 * - no persistence technology
 * - no DTOs
 */
public interface OfferRepository {

    Mono<Offer> save(Offer offer);

    Mono<Offer> findById(IdValue id);

    /**
     * Returns the ACTIVE offer for a given resource in a given community.
     *
     * Useful for enforcing the invariant:
     * - at most one ACTIVE offer by (tenantId, communityId, resourceId)
     */
    Mono<Offer> findActiveByTenantIdAndCommunityIdAndResourceId(
            IdValue tenantId,
            IdValue communityId,
            IdValue resourceId
    );

    /**
     * Returns all offers owned by a user inside a tenant.
     *
     * Useful for:
     * - C6 ListMyOffers
     */
    Flux<Offer> findByTenantIdAndOwnerId(
            IdValue tenantId,
            IdValue ownerId
    );

    /**
     * Generic paginated/filterable search.
     *
     * Useful for future listing endpoints if the project keeps
     * a common SearchQueryValue-based strategy.
     */
    Flux<Offer> search(SearchQueryValue query);

    /**
     * Total number of results matching the same search criteria.
     *
     * Used together with search(...) for pagination metadata.
     */
    Mono<Long> count(SearchQueryValue query);
}