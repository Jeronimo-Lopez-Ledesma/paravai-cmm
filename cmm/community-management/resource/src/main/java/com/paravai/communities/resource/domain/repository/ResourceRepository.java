package com.paravai.communities.resource.domain.repository;

import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain port for Resource aggregate.
 *
 * Hexagonal Architecture:
 * - no Spring
 * - no persistence technology
 * - no DTOs
 */
public interface ResourceRepository {

    Mono<Resource> save(Resource resource);

    Mono<Resource> findById(IdValue id);

    /**
     * Returns a resource only if it belongs to the given owner.
     *
     * Useful for ownership-safe reads in application services.
     */
    Mono<Resource> findByIdAndOwnerId(IdValue id, IdValue ownerId);

    /**
     * Returns all resources owned by a user inside a tenant.
     *
     * Useful for "ListMyResources".
     */
    Flux<Resource> findByTenantIdAndOwnerId(
            IdValue tenantId,
            IdValue ownerId
    );

    /**
     * Generic paginated/filterable search.
     *
     * Useful for future listing endpoints if the project keeps
     * a common SearchQueryValue-based strategy.
     */
    Flux<Resource> search(SearchQueryValue query);

    /**
     * Total number of results matching the same search criteria.
     *
     * Used together with search(...) for pagination metadata.
     */
    Mono<Long> count(SearchQueryValue query);
}