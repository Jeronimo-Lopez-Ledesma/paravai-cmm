package com.paravai.communities.membership.domain.repository;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain port for Membership aggregate.
 *
 * Hexagonal Architecture:
 * - no Spring
 * - no persistence technology
 * - no DTOs
 */
public interface MembershipRepository {

    Mono<Membership> save(Membership membership);

    Mono<Membership> findById(IdValue id);

    /**
     * Finds the membership relationship by its business key.
     *
     * Business key:
     * - (tenantId, communityId, userId)
     */
    Mono<Membership> findByTenantIdAndCommunityIdAndUserId(
            IdValue tenantId,
            IdValue communityId,
            IdValue userId
    );

    /**
     * Returns all memberships of a community.
     *
     * Useful for administrative checks and future queries.
     */
    Flux<Membership> findByTenantIdAndCommunityId(
            IdValue tenantId,
            IdValue communityId
    );

    /**
     * Counts active administrators in a community.
     *
     * Needed to enforce the invariant:
     * - a community must keep at least one ACTIVE ADMIN
     */
    Mono<Long> countActiveAdmins(
            IdValue tenantId,
            IdValue communityId
    );

    /**
     * Generic paginated/filterable search.
     *
     * Used for listing memberships with filters, search text, sorting and pagination.
     */
    Flux<Membership> search(SearchQueryValue query);

    /**
     * Total number of results matching the same search criteria.
     *
     * Used together with search(...) for pagination metadata.
     */
    Mono<Long> count(SearchQueryValue query);
}