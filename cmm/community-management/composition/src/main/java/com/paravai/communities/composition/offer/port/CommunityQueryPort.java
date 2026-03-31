package com.paravai.communities.composition.offer.port;

import reactor.core.publisher.Mono;

public interface CommunityQueryPort {

    Mono<CommunitySummary> findById(String communityId);
}