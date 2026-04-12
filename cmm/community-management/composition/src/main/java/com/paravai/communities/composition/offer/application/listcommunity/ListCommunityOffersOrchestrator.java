package com.paravai.communities.composition.offer.application.listcommunity;

import com.paravai.communities.composition.offer.application.exception.CommunityNotFoundException;
import com.paravai.communities.composition.offer.application.exception.UnauthenticatedUserException;
import com.paravai.communities.composition.offer.application.exception.UserNotActiveException;
import com.paravai.communities.composition.offer.application.exception.UserNotMemberException;
import com.paravai.communities.composition.offer.port.CommunityQueryPort;
import com.paravai.communities.composition.offer.port.MembershipQueryPort;
import com.paravai.communities.composition.offer.port.OfferQueryPort;
import com.paravai.communities.composition.offer.port.OfferSummary;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class ListCommunityOffersOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ListCommunityOffersOrchestrator.class);

    private final CommunityQueryPort communityPort;
    private final MembershipQueryPort membershipPort;
    private final OfferQueryPort offerQueryPort;

    public ListCommunityOffersOrchestrator(
            CommunityQueryPort communityPort,
            MembershipQueryPort membershipPort,
            OfferQueryPort offerQueryPort
    ) {
        this.communityPort = Objects.requireNonNull(communityPort, "communityPort");
        this.membershipPort = Objects.requireNonNull(membershipPort, "membershipPort");
        this.offerQueryPort = Objects.requireNonNull(offerQueryPort, "offerQueryPort");
    }

    public Flux<OfferSummary> list(
            String tenantId,
            String communityId,
            int page,
            int size
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");

        return Flux.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info("[{}][{}][{}] ListCommunityOffers context in composition - tenantId={}, communityId={}, page={}, size={}",
                    traceId,
                    userOid,
                    sourceSystem,
                    tenantId,
                    communityId,
                    page,
                    size
            );

            if (userOid == null || userOid.isBlank() || "anonymous".equals(userOid)) {
                return Flux.error(new UnauthenticatedUserException());
            }

            Mono<Void> validateCommunity =
                    communityPort.findOfferPolicy(communityId)
                            .switchIfEmpty(Mono.error(new CommunityNotFoundException(communityId)))
                            .then();

            Mono<Void> validateMembership =
                    membershipPort.findByUserInCommunity(
                                    tenantId,
                                    communityId,
                                    userOid
                            )
                            .switchIfEmpty(Mono.error(
                                    new UserNotMemberException(userOid, communityId)
                            ))
                            .flatMap(membership -> membership.isActive()
                                    ? Mono.<Void>empty()
                                    : Mono.error(new UserNotActiveException(userOid, communityId))
                            );

            return Mono.when(validateCommunity, validateMembership)
                    .thenMany(
                            offerQueryPort.listCommunityOffers(
                                    tenantId,
                                    communityId,
                                    page,
                                    size
                            )
                    )
                    .doOnComplete(() ->
                            log.info("[{}][{}] Community offers retrieved for community {}",
                                    traceId, userOid, communityId)
                    )
                    .doOnError(e -> {
                        if (e instanceof com.paravai.foundation.domain.exception.CustomException) {
                            log.warn("[{}][{}] ListCommunityOffers business error: {}",
                                    traceId, userOid, e.getMessage());
                        } else {
                            log.error("[{}][{}] ListCommunityOffers failed",
                                    traceId, userOid, e);
                        }
                    });
        });
    }

    public Mono<Long> count(
            String tenantId,
            String communityId
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info("[{}][{}][{}] CountCommunityOffers context in composition - tenantId={}, communityId={}",
                    traceId,
                    userOid,
                    sourceSystem,
                    tenantId,
                    communityId
            );

            if (userOid == null || userOid.isBlank() || "anonymous".equals(userOid)) {
                return Mono.error(new UnauthenticatedUserException());
            }

            Mono<Void> validateCommunity =
                    communityPort.findOfferPolicy(communityId)
                            .switchIfEmpty(Mono.error(new CommunityNotFoundException(communityId)))
                            .then();

            Mono<Void> validateMembership =
                    membershipPort.findByUserInCommunity(
                                    tenantId,
                                    communityId,
                                    userOid
                            )
                            .switchIfEmpty(Mono.error(
                                    new UserNotMemberException(userOid, communityId)
                            ))
                            .flatMap(membership -> membership.isActive()
                                    ? Mono.<Void>empty()
                                    : Mono.error(new UserNotActiveException(userOid, communityId))
                            );

            return Mono.when(validateCommunity, validateMembership)
                    .then(
                            offerQueryPort.countCommunityOffers(
                                    tenantId,
                                    communityId
                            )
                    )
                    .doOnSuccess(total ->
                            log.info("[{}][{}] Community offers total={} for community {}",
                                    traceId, userOid, total, communityId)
                    )
                    .doOnError(e -> {
                        if (e instanceof com.paravai.foundation.domain.exception.CustomException) {
                            log.warn("[{}][{}] CountCommunityOffers business error: {}",
                                    traceId, userOid, e.getMessage());
                        } else {
                            log.error("[{}][{}] CountCommunityOffers failed",
                                    traceId, userOid, e);
                        }
                    });
        });
    }
}