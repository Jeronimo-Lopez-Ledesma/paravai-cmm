package com.paravai.communities.community.domain.port;

import com.paravai.foundation.domain.value.IdValue;
import reactor.core.publisher.Mono;

/**
 * Domain/Application port.
 * Community BC must not depend on Membership persistence.
 */
public interface CommunityAuthorizationPort {

    /**
     * @return true if user has admin-like privileges in the community (ADMIN or OWNER).
     */
    Mono<Boolean> isCommunityAdmin(IdValue tenantId, IdValue communityId, IdValue userId);
}