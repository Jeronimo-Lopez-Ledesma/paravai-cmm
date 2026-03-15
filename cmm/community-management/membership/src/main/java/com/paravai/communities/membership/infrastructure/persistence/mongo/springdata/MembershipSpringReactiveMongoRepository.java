package com.paravai.communities.membership.infrastructure.persistence.mongo.springdata;

import com.paravai.communities.membership.infrastructure.persistence.mongo.document.MembershipDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data Reactive repository for MembershipDocument.
 *
 * Infrastructure-only.
 * No domain objects here.
 */
public interface MembershipSpringReactiveMongoRepository
        extends ReactiveMongoRepository<MembershipDocument, String> {

    /**
     * Uniqueness / existence check for business key:
     * (tenantId + communityId + userId).
     */
    Mono<Boolean> existsByTenantIdAndCommunityIdAndUserId(String tenantId, String communityId, String userId);

    /**
     * Lookup by business key (very common for permission checks).
     */
    Mono<MembershipDocument> findByTenantIdAndCommunityIdAndUserId(String tenantId, String communityId, String userId);

    Flux<MembershipDocument> findByTenantIdAndCommunityId(
            String tenantId,
            String communityId
    );

    Mono<Long> countByTenantIdAndCommunityIdAndStatusCodeAndRoleCode(
            String tenantId,
            String communityId,
            String statusCode,
            String roleCode
    );
}