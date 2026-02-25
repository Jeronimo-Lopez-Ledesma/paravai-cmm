package com.paravai.communities.community.infrastructure.security;

import com.paravai.communities.community.domain.port.CommunityAuthorizationPort;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * MVP stub: allows everything.
 * Replace with Membership-based implementation later.
 */
@Component
public class AllowAllCommunityAuthorizationAdapter implements CommunityAuthorizationPort {

    @Override
    public Mono<Boolean> isCommunityAdmin(IdValue tenantId, IdValue communityId, IdValue userId) {
        return Mono.just(true);
    }
}