package com.paravai.communities.resource.application.query.get;

import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.domain.repository.ResourceRepository;
import com.paravai.foundation.domain.value.IdValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class GetResourceService {

    private static final Logger log = LoggerFactory.getLogger(GetResourceService.class);

    private final ResourceRepository repo;

    public GetResourceService(ResourceRepository repo) {
        this.repo = Objects.requireNonNull(repo, "repo");
    }

    public Mono<GetResourceResult> get(
            IdValue resourceId,
            IdValue ownerId
    ) {
        Objects.requireNonNull(resourceId, "resourceId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        return repo.findByIdAndOwnerId(resourceId, ownerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Resource not found")))
                .map(GetResourceResult::found);
    }
}