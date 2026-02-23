package com.paravai.communities.community.domain.repository;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain port (Hexagonal Architecture).
 * No Spring, no Mongo, no DTOs.
 */
public interface CommunityRepository {

    Mono<Community> save(Community community);

    Mono<Community> findById(IdValue id);

    Mono<Void> deleteById(IdValue id);

    /**
     * Uniqueness check for business identity (MVP: slug uniqueness within tenant).
     * Used by application services for AC4 (409 Conflict).
     */
    Mono<Boolean> existsByTenantIdAndSlug(IdValue tenantId, String slug);

    // Search with pagination (filters + search + sort + pagination)
    Flux<Community> search(SearchQueryValue query);

    // Needed for pagination
    Mono<Long> count(SearchQueryValue query);
}