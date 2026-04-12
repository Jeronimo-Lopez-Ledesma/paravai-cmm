package com.paravai.communities.composition.offer.application.listmy;

import com.paravai.communities.composition.offer.port.OfferQueryPort;
import com.paravai.communities.composition.offer.port.OfferSummary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class ListMyOffersOrchestrator {

    private final OfferQueryPort offerQueryPort;

    public ListMyOffersOrchestrator(OfferQueryPort offerQueryPort) {
        this.offerQueryPort = Objects.requireNonNull(offerQueryPort, "offerQueryPort");
    }

    public Flux<OfferSummary> list(
            String tenantId,
            String ownerId,
            String status,
            int page,
            int size
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        return offerQueryPort.listMine(
                tenantId,
                ownerId,
                normalizeStatus(status),
                page,
                size
        );
    }

    public Mono<Long> count(
            String tenantId,
            String ownerId,
            String status
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        return offerQueryPort.countMine(
                tenantId,
                ownerId,
                normalizeStatus(status)
        );
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return status.trim().toUpperCase();
    }
}