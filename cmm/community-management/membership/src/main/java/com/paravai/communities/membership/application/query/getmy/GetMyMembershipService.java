package com.paravai.communities.membership.application.query.getmy;

import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class GetMyMembershipService {

    private final MembershipRepository repo;

    public GetMyMembershipService(MembershipRepository repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }

    public Mono<GetMyMembershipResult> getMyMembership(
            IdValue tenantId,
            IdValue communityId,
            IdValue userId
    ) {
        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(communityId);
        Objects.requireNonNull(userId);

        return repo.findByTenantIdAndCommunityIdAndUserId(
                        tenantId,
                        communityId,
                        userId
                )
                .map(GetMyMembershipResult::fromMembership)
                .defaultIfEmpty(GetMyMembershipResult.none());
    }
}