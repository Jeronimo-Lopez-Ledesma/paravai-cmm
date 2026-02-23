package com.paravai.regulations.standards.relationships.domain.repository;

import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import com.paravai.regulations.standards.relationships.domain.model.StandardRelationship;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain repository for StandardRelationship aggregate.
 *
 * Mutations supported:
 * - create (save)
 * - delete
 *
 * Reads supported:
 * - findById
 * - search + count (for pagination)
 *
 * No update operation is defined.
 */
public interface StandardRelationshipRepository {

    /**
     * Persists a new StandardRelationship.
     *
     * Implementations must enforce business uniqueness:
     * (from, type, purpose, to).
     */
    Mono<StandardRelationship> save(StandardRelationship relationship);

    /**
     * Find a relationship by its aggregate identifier.
     */
    Mono<StandardRelationship> findById(IdValue id);

    /**
     * Deletes a relationship by its aggregate identifier.
     *
     * Operation is idempotent.
     */
    Mono<Void> deleteById(IdValue id);

    /**
     * Search relationships using ServiceFoundation SearchQueryValue
     * (filters + search + sort + pagination).
     *
     * Expected filters (convention):
     * - fromStandardId
     * - fromVersionId
     * - toStandardId
     * - toVersionId
     * - type
     * - purpose
     */
    Flux<StandardRelationship> search(SearchQueryValue q);

    /**
     * Count relationships matching the same criteria (pagination metadata).
     */
    Mono<Long> count(SearchQueryValue q);
}
