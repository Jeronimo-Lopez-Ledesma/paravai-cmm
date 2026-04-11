package com.paravai.communities.composition.offer.application.updateavailability;

import com.paravai.communities.composition.offer.application.exception.UnauthenticatedUserException;
import com.paravai.communities.composition.offer.port.OfferCommandPort;
import com.paravai.communities.composition.offer.port.UpdateOfferAvailabilityCommand;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class UpdateOfferAvailabilityOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(UpdateOfferAvailabilityOrchestrator.class);

    private final OfferCommandPort offerCommandPort;

    public UpdateOfferAvailabilityOrchestrator(OfferCommandPort offerCommandPort) {
        this.offerCommandPort = Objects.requireNonNull(offerCommandPort);
    }

    public Mono<UpdateOfferAvailabilityResult> update(
            IdValue offerId,
            String availabilityStatusCode
    ) {
        return Mono.deferContextual(ctx -> {

            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info("[{}][{}][{}] UpdateOfferAvailability context in composition - offerId={}, availabilityStatusCode={}",
                    traceId,
                    userOid,
                    sourceSystem,
                    offerId.value(),
                    availabilityStatusCode
            );

            if (userOid == null || userOid.isBlank() || "anonymous".equals(userOid)) {
                return Mono.error(new UnauthenticatedUserException());
            }

            log.debug("[{}][{}] UpdateOfferAvailability start", traceId, userOid);

            UpdateOfferAvailabilityCommand command = new UpdateOfferAvailabilityCommand(
                    offerId.value(),
                    availabilityStatusCode
            );

            return offerCommandPort.updateOfferAvailability(command)
                    .map(UpdateOfferAvailabilityResult::updated)
                    .doOnSuccess(r ->
                            log.info("[{}][{}] Offer availability updated {}", traceId, userOid, r.offer().offerId())
                    )
                    .doOnError(e -> {
                        if (e instanceof com.paravai.foundation.domain.exception.CustomException) {
                            log.warn("[{}][{}] UpdateOfferAvailability business error: {}", traceId, userOid, e.getMessage());
                        } else {
                            log.error("[{}][{}] UpdateOfferAvailability failed", traceId, userOid, e);
                        }
                    });
        });
    }
}