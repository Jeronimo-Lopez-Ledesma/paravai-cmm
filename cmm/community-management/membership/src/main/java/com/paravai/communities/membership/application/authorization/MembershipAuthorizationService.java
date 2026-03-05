package com.paravai.communities.membership.application.authorization;

import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MembershipAuthorizationService {

    private final MembershipRepository repo;

    public MembershipAuthorizationService(MembershipRepository repo) {
        this.repo = repo;
    }

    public Mono<Void> assertAdmin(IdValue tenantId, IdValue communityId, IdValue userId) {
        return repo.findByTenantIdAndCommunityIdAndUserId(tenantId, communityId, userId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Membership not found")))
                .flatMap(m -> {
                    if (!m.status().isActive()) {
                        return Mono.error(new IllegalArgumentException("Membership not active"));
                    }
                    if (!m.role().isAdmin() && !m.role().isOwner()) {
                        return Mono.error(new IllegalArgumentException("User is not admin"));
                    }
                    return Mono.empty();
                });
    }
}