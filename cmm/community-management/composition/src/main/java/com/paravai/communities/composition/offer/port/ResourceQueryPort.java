package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Mono;

public interface ResourceQueryPort {

    Mono<ResourceSummary> findOwnedById(
            String resourceId,
            String ownerId
    );
}