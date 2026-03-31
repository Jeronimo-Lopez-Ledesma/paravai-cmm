package com.paravai.communities.offer.infrastructure.persistence.mongo.springdata;

import com.paravai.communities.offer.infrastructure.persistence.mongo.document.OfferDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data Reactive repository for OfferDocument.
 *
 * Infrastructure-only.
 * No domain objects here.
 */
public interface OfferSpringReactiveMongoRepository
        extends ReactiveMongoRepository<OfferDocument, String> {

    /**
     * Returns the ACTIVE offer for a given resource in a given community.
     *
     * Useful for:
     * - enforcing the invariant of one ACTIVE offer by (tenantId, communityId, resourceId)
     */
    Mono<OfferDocument> findByTenantIdAndCommunityIdAndResourceIdAndStatusCode(
            String tenantId,
            String communityId,
            String resourceId,
            String statusCode
    );

    /**
     * Returns all offers owned by a user within a tenant.
     *
     * Useful for:
     * - C6 ListMyOffers
     */
    Flux<OfferDocument> findByTenantIdAndOwnerId(
            String tenantId,
            String ownerId
    );
}