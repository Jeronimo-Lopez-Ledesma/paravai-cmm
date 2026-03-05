package com.paravai.communities.membership.application.query;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FindMembershipByCommunityAndUserService {
    private final MembershipRepository repo;
    public FindMembershipByCommunityAndUserService(MembershipRepository repo) { this.repo = repo; }

    public Mono<Membership> find(IdValue tenantId, IdValue communityId, IdValue userId) {
        if (tenantId == null || communityId == null || userId == null) {
            return Mono.error(new IllegalArgumentException("tenantId/communityId/userId cannot be null"));
        }
        return repo.findByTenantIdAndCommunityIdAndUserId(tenantId, communityId, userId);
    }
}