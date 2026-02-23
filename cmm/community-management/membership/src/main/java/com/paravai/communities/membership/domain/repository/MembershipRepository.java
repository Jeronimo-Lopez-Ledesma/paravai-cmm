package com.paravai.communities.membership.domain.repository;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain port (Hexagonal Architecture).
 * No Spring, no Mongo, no DTOs.
 */
public interface MembershipRepository {

    Mono<Membership> save(Membership membership);

    Mono<Membership> findById(IdValue id);

    Mono<Void> deleteById(IdValue id);

    /**
     * Domain uniqueness constraint:
     * - A user can have only one membership record per (tenantId, communityId).
     */
    Mono<Boolean> existsByTenantIdAndCommunityIdAndUserId(IdValue tenantId, IdValue communityId, IdValue userId);

    /**
     * Find membership by business key (very common for permission checks).
     */
    Mono<Membership> findByTenantIdAndCommunityIdAndUserId(IdValue tenantId, IdValue communityId, IdValue userId);

    // Search with pagination (filters + search + sort + pagination)
    Flux<Membership> search(SearchQueryValue query);

    // Needed for pagination
    Mono<Long> count(SearchQueryValue query);
}