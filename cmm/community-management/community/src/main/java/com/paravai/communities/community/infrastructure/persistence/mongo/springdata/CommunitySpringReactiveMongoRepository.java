package com.paravai.communities.community.infrastructure.persistence.mongo.springdata;

import com.paravai.communities.community.infrastructure.persistence.mongo.document.CommunityDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Spring Data Reactive repository for CommunityDocument.
 *
 * Infrastructure-only.
 * No domain objects here.
 */
public interface CommunitySpringReactiveMongoRepository
        extends ReactiveMongoRepository<CommunityDocument, String> {

    /**
     * Used for business identity uniqueness validation (tenantId + slug).
     * EPIC A - AC4.
     */
    Mono<Boolean> existsByTenantIdAndSlug(String tenantId, String slug);

    /**
     * Optional but useful for application-level validation
     * (e.g., detect conflict with different id).
     */
    Mono<CommunityDocument> findByTenantIdAndSlug(String tenantId, String slug);
}