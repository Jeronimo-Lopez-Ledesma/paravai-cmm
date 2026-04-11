package com.paravai.communities.composition.offer.application.withdraw;

import com.paravai.communities.composition.offer.application.exception.UnauthenticatedUserException;
import com.paravai.communities.composition.offer.port.OfferCommandPort;
import com.paravai.communities.composition.offer.port.WithdrawOfferCommand;
import com.paravai.communities.composition.offer.port.OfferSummary;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class WithdrawOfferOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(WithdrawOfferOrchestrator.class);

    private final OfferCommandPort offerCommandPort;

    public WithdrawOfferOrchestrator(OfferCommandPort offerCommandPort) {
        this.offerCommandPort = Objects.requireNonNull(offerCommandPort);
    }

    public Mono<OfferSummary> withdraw(IdValue offerId) {
        return Mono.deferContextual(ctx -> {

            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info("[{}][{}][{}] WithdrawOffer context in composition - offerId={}",
                    traceId,
                    userOid,
                    sourceSystem,
                    offerId.value()
            );

            if (userOid == null || userOid.isBlank() || "anonymous".equals(userOid)) {
                return Mono.error(new UnauthenticatedUserException());
            }

            log.debug("[{}][{}] WithdrawOffer start", traceId, userOid);

            WithdrawOfferCommand command = new WithdrawOfferCommand(
                    offerId.value()
            );

            return offerCommandPort.withdrawOffer(command)
                    .doOnSuccess(summary ->
                            log.info("[{}][{}] Offer withdrawn {}", traceId, userOid, summary.offerId())
                    )
                    .doOnError(e -> {
                        if (e instanceof com.paravai.foundation.domain.exception.CustomException) {
                            log.warn("[{}][{}] WithdrawOffer business error: {}", traceId, userOid, e.getMessage());
                        } else {
                            log.error("[{}][{}] WithdrawOffer failed", traceId, userOid, e);
                        }
                    });
        });
    }
}