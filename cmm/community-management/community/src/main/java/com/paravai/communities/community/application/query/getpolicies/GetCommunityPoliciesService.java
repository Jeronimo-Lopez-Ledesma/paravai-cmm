package com.paravai.communities.community.application.query.getpolicies;

import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.repository.CommunityRepository;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class GetCommunityPoliciesService {

    private final CommunityRepository repo;

    public GetCommunityPoliciesService(CommunityRepository repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }

    public Mono<Community> getPolicies(IdValue communityId) {
        Objects.requireNonNull(communityId, "communityId is required");

        return repo.findById(communityId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Community not found")));
    }
}