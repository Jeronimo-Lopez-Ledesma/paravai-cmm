package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Mono;

public interface MembershipQueryPort {

    Mono<MembershipSummary> findByTenantAndCommunityAndUser(
            String tenantId,
            String communityId,
            String userId
    );
}