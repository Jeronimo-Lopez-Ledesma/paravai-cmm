package com.paravai.communities.resource.infrastructure.persistence.mongo.springdata;

import com.paravai.communities.resource.infrastructure.persistence.mongo.document.ResourceDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data Reactive repository for ResourceDocument.
 *
 * Infrastructure-only.
 * No domain objects here.
 */
public interface ResourceSpringReactiveMongoRepository
        extends ReactiveMongoRepository<ResourceDocument, String> {

    /**
     * Ownership-safe lookup.
     *
     * Useful for:
     * - GET by id with ownership isolation
     * - future command-side validations
     */
    Mono<ResourceDocument> findByIdAndOwnerId(String id, String ownerId);

    /**
     * Returns all resources owned by a user within a tenant.
     *
     * Useful for:
     * - C6 ListMyResources (non-paginated scenarios)
     */
    Flux<ResourceDocument> findByTenantIdAndOwnerId(
            String tenantId,
            String ownerId
    );

    /**
     * Returns a page of resources owned by a user within a tenant.
     *
     * Useful for:
     * - C6 ListMyResources with pagination
     *
     * Notes:
     * - pagination is delegated to Spring Data via Pageable
     */
    Flux<ResourceDocument> findByTenantIdAndOwnerId(
            String tenantId,
            String ownerId,
            Pageable pageable
    );

    /**
     * Returns the total number of resources owned by a user within a tenant.
     *
     * Useful for:
     * - building pagination metadata (total elements)
     */
    Mono<Long> countByTenantIdAndOwnerId(
            String tenantId,
            String ownerId
    );
}