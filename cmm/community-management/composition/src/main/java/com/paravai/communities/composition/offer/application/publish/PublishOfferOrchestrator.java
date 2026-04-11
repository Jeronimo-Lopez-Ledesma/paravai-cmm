package com.paravai.communities.composition.offer.application.publish;

import com.paravai.communities.composition.offer.application.exception.CommunityNotFoundException;
import com.paravai.communities.composition.offer.application.exception.ExchangeTypeNotAllowedException;
import com.paravai.communities.composition.offer.application.exception.ResourceNotFoundException;
import com.paravai.communities.composition.offer.application.exception.UnauthenticatedUserException;
import com.paravai.communities.composition.offer.application.exception.UserNotActiveException;
import com.paravai.communities.composition.offer.application.exception.UserNotMemberException;
import com.paravai.communities.composition.offer.port.CommunityQueryPort;
import com.paravai.communities.composition.offer.port.CommunitySummary;
import com.paravai.communities.composition.offer.port.CreateOfferCommand;
import com.paravai.communities.composition.offer.port.MembershipQueryPort;
import com.paravai.communities.composition.offer.port.MembershipSummary;
import com.paravai.communities.composition.offer.port.OfferCommandPort;
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
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info("[{}][{}][{}] PublishOffer context in composition - tenantId={}, communityId={}, resourceId={}",
                    traceId,
                    userOid,
                    sourceSystem,
                    tenantId.value(),
                    communityId.value(),
                    resourceId.value()
            );


            if (userOid == null || userOid.isBlank()) {
                return Mono.error(new UnauthenticatedUserException());
            }

            log.debug("[{}][{}] PublishOffer start", traceId, userOid);

            Mono<CommunitySummary> communityMono =
                    communityPort.findOfferPolicy(communityId.value())
                            .switchIfEmpty(Mono.error(
                                    new CommunityNotFoundException(communityId.value())
                            ));

            Mono<MembershipSummary> membershipMono =
                    membershipPort.findByUserInCommunity(
                                    tenantId.value(),
                                    communityId.value(),
                                    actingUserId.value()
                            )
                            .switchIfEmpty(Mono.error(
                                    new UserNotMemberException(actingUserId.value(), communityId.value())
                            ))
                            .flatMap(m -> m.isActive()
                                            ? Mono.just(m)
                                            : Mono.error(new UserNotActiveException(
                                            actingUserId.value(),
                                            communityId.value()
                                    ))
                            );

            Mono<ResourceSummary> resourceMono =
                    resourcePort.findOwnedById(
                                    resourceId.value(),
                                    actingUserId.value()
                            )
                            .switchIfEmpty(Mono.error(
                                    new ResourceNotFoundException(resourceId.value())
                            ));

            return Mono.zip(communityMono, membershipMono, resourceMono)
                    .flatMap(tuple -> {

                        CommunitySummary community = tuple.getT1();
                        MembershipSummary membership = tuple.getT2();
                        ResourceSummary resource = tuple.getT3();

                        log.info(
                                "[{}][{}] Community {} allows exchange types: {}. Requested: {}",
                                traceId,
                                userOid,
                                community.communityId(),
                                community.allowedExchangeTypes(),
                                exchangeTypeCode
                        );

                        if (!community.allowsExchangeType(exchangeTypeCode)) {
                            return Mono.error(new ExchangeTypeNotAllowedException(
                                    exchangeTypeCode,
                                    community.communityId()
                            ));
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
                    .doOnError(e -> {
                        if (e instanceof com.paravai.foundation.domain.exception.CustomException) {
                            log.warn("[{}][{}] PublishOffer business error: {}", traceId, userOid, e.getMessage());
                        } else {
                            log.error("[{}][{}] PublishOffer failed", traceId, userOid, e);
                        }
                    });
        });
    }
}