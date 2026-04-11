package com.paravai.communities.composition.offer.application.pause;

import com.paravai.communities.composition.offer.application.exception.UnauthenticatedUserException;
import com.paravai.communities.composition.offer.port.OfferCommandPort;
import com.paravai.communities.composition.offer.port.PauseOfferCommand;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class PauseOfferOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PauseOfferOrchestrator.class);

    private final OfferCommandPort offerCommandPort;

    public PauseOfferOrchestrator(OfferCommandPort offerCommandPort) {
        this.offerCommandPort = Objects.requireNonNull(offerCommandPort);
    }

    public Mono<PauseOfferResult> pause(IdValue offerId) {
        return Mono.deferContextual(ctx -> {

            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info("[{}][{}][{}] PauseOffer context in composition - offerId={}",
                    traceId,
                    userOid,
                    sourceSystem,
                    offerId.value()
            );

            if (userOid == null || userOid.isBlank() || "anonymous".equals(userOid)) {
                return Mono.error(new UnauthenticatedUserException());
            }

            log.debug("[{}][{}] PauseOffer start", traceId, userOid);

            PauseOfferCommand command = new PauseOfferCommand(
                    offerId.value()
            );

            return offerCommandPort.pauseOffer(command)
                    .map(PauseOfferResult::updated)
                    .doOnSuccess(r ->
                            log.info("[{}][{}] Offer paused {}", traceId, userOid, r.offer().offerId())
                    )
                    .doOnError(e -> {
                        if (e instanceof com.paravai.foundation.domain.exception.CustomException) {
                            log.warn("[{}][{}] PauseOffer business error: {}", traceId, userOid, e.getMessage());
                        } else {
                            log.error("[{}][{}] PauseOffer failed", traceId, userOid, e);
                        }
                    });
        });
    }
}