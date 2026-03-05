package com.paravai.communities.membership.application.query;

import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.value.IdValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FindMembershipByIdService {
    private final MembershipRepository repo;
    public FindMembershipByIdService(MembershipRepository repo) { this.repo = repo; }

    public Mono<Membership> findById(IdValue id) {
        if (id == null) return Mono.error(new IllegalArgumentException("id cannot be null"));
        return repo.findById(id);
    }
}