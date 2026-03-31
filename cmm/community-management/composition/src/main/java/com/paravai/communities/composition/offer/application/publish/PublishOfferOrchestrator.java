package com.paravai.communities.composition.offer.application.publish;

import com.paravai.communities.composition.offer.port.*;
import com.paravai.communities.offer.application.command.create.CreateOfferService;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class PublishOfferOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PublishOfferOrchestrator.class);

    private final CommunityQueryPort communityPort;
    private final MembershipQueryPort membershipPort;
    private final ResourceQueryPort resourcePort;
    private final CreateOfferService createOfferService;

    public PublishOfferOrchestrator(
            CommunityQueryPort communityPort,
            MembershipQueryPort membershipPort,
            ResourceQueryPort resourcePort,
            CreateOfferService createOfferService
    ) {
        this.communityPort = Objects.requireNonNull(communityPort);
        this.membershipPort = Objects.requireNonNull(membershipPort);
        this.resourcePort = Objects.requireNonNull(resourcePort);
        this.createOfferService = Objects.requireNonNull(createOfferService);
    }

    public Mono<PublishOfferResult> publish(
            IdValue tenantId,
            IdValue communityId,
            IdValue resourceId,
            IdValue actingUserId,
            String exchangeTypeCode,
            String description
    ) {

        return Mono.deferContextual(ctx -> {

            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);

            log.debug("[{}][{}] PublishOffer start", traceId, userOid);

            Mono<CommunitySummary> communityMono =
                    communityPort.findById(communityId.value())
                            .switchIfEmpty(Mono.error(
                                    new IllegalArgumentException("Community not found")
                            ));

            Mono<MembershipSummary> membershipMono =
                    membershipPort.findByTenantAndCommunityAndUser(
                                    tenantId.value(),
                                    communityId.value(),
                                    actingUserId.value()
                            )
                            .switchIfEmpty(Mono.error(
                                    new IllegalArgumentException("User is not a member of the community")
                            ))
                            .flatMap(m -> m.isActive()
                                    ? Mono.just(m)
                                    : Mono.error(new IllegalArgumentException("User is not ACTIVE"))
                            );

            Mono<ResourceSummary> resourceMono =
                    resourcePort.findOwnedById(
                                    resourceId.value(),
                                    actingUserId.value()
                            )
                            .switchIfEmpty(Mono.error(
                                    new IllegalArgumentException("Resource not found")
                            ));

            return Mono.zip(communityMono, membershipMono, resourceMono)
                    .flatMap(tuple -> {

                        CommunitySummary community = tuple.getT1();

                        // regla comunidad
                        if (!community.allows(exchangeTypeCode)) {
                            return Mono.error(new IllegalArgumentException(
                                    "Exchange type not allowed"
                            ));
                        }

                        return createOfferService.create(
                                        tenantId,
                                        communityId,
                                        resourceId,
                                        actingUserId,
                                        exchangeTypeCode,
                                        description
                                )
                                .map(r -> PublishOfferResult.created(r.offer()));
                    })
                    .doOnSuccess(r ->
                            log.info("[{}][{}] Offer published {}", traceId, userOid, r.offer().id())
                    )
                    .doOnError(e ->
                            log.error("[{}][{}] PublishOffer failed", traceId, userOid, e)
                    );
        });
    }
}