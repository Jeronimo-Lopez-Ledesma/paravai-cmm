package com.paravai.communities.composition.offer.application.publish;

import com.paravai.communities.composition.offer.port.CommunityQueryPort;
import com.paravai.communities.composition.offer.port.CommunitySummary;
import com.paravai.communities.composition.offer.port.CreateOfferCommand;
import com.paravai.communities.composition.offer.port.MembershipQueryPort;
import com.paravai.communities.composition.offer.port.MembershipSummary;
import com.paravai.communities.composition.offer.port.OfferCommandPort;
import com.paravai.communities.composition.offer.port.OfferSummary;
import com.paravai.communities.composition.offer.port.ResourceQueryPort;
import com.paravai.communities.composition.offer.port.ResourceSummary;
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
    private final OfferCommandPort offerCommandPort;

    public PublishOfferOrchestrator(
            CommunityQueryPort communityPort,
            MembershipQueryPort membershipPort,
            ResourceQueryPort resourcePort,
            OfferCommandPort offerCommandPort
    ) {
        this.communityPort = Objects.requireNonNull(communityPort);
        this.membershipPort = Objects.requireNonNull(membershipPort);
        this.resourcePort = Objects.requireNonNull(resourcePort);
        this.offerCommandPort = Objects.requireNonNull(offerCommandPort);
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
                    communityPort.findOfferPolicy(communityId.value())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Community not found")));

            Mono<MembershipSummary> membershipMono =
                    membershipPort.findByUserInCommunity(
                                    tenantId.value(),
                                    communityId.value(),
                                    actingUserId.value()
                            )
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("User is not a member")))
                            .flatMap(m -> m.isActive()
                                    ? Mono.just(m)
                                    : Mono.error(new IllegalArgumentException("User is not ACTIVE"))
                            );

            Mono<ResourceSummary> resourceMono =
                    resourcePort.findOwnedById(
                                    resourceId.value(),
                                    actingUserId.value()
                            )
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Resource not found")));

            return Mono.zip(communityMono, membershipMono, resourceMono)
                    .flatMap(tuple -> {

                        CommunitySummary community = tuple.getT1();
                        log.info("[{}][{}] Community {} allows exchange types: {}. Requested: {}",
                                traceId,
                                userOid,
                                community.communityId(),
                                community.allowedExchangeTypes(),
                                exchangeTypeCode
                        );
                        if (!community.allowsExchangeType(exchangeTypeCode)) {
                            return Mono.error(new IllegalArgumentException("Exchange type not allowed"));
                        }

                        CreateOfferCommand command = new CreateOfferCommand(
                                tenantId.value(),
                                communityId.value(),
                                resourceId.value(),
                                actingUserId.value(),
                                exchangeTypeCode,
                                description
                        );

                        return offerCommandPort.createOffer(command)
                                .map(PublishOfferResult::created);
                    })
                    .doOnSuccess(r ->
                            log.info("[{}][{}] Offer published {}", traceId, userOid, r.offer().offerId())
                    )
                    .doOnError(e ->
                            log.error("[{}][{}] PublishOffer failed", traceId, userOid, e)
                    );
        });
    }
}