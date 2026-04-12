package com.paravai.communities.offer.application.query.listmy;

import com.paravai.communities.offer.application.common.OfferMetrics;
import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.domain.repository.OfferRepository;
import com.paravai.communities.offer.domain.value.OfferStatusValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class ListMyOffersService {

    private static final Logger log = LoggerFactory.getLogger(ListMyOffersService.class);

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final OfferRepository repo;
    private final ReactiveOperationMetrics metrics;

    public ListMyOffersService(
            OfferRepository repo,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Flux<Offer> list(
            IdValue tenantId,
            IdValue ownerId,
            String statusCode,
            Integer page,
            Integer size
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);
        OfferStatusValue status = normalizeStatus(statusCode);

        return Flux.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    OfferMetrics.ID.app("listMyOffers"),
                    sourceSystem
            );

            return MetricsSupport.timedFlux(metrics, opCtx, () -> {
                Flux<Offer> query = (status == null)
                        ? repo.findByTenantIdAndOwnerId(tenantId, ownerId, effectivePage, effectiveSize)
                        : repo.findByTenantIdAndOwnerIdAndStatus(tenantId, ownerId, status, effectivePage, effectiveSize);

                return query.doOnError(ex -> log.error(
                        "[{}][{}] Failed to list offers for owner {}",
                        traceId,
                        userOid,
                        ownerId,
                        ex
                ));
            });
        });
    }

    public Mono<Long> count(
            IdValue tenantId,
            IdValue ownerId,
            String statusCode
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        OfferStatusValue status = normalizeStatus(statusCode);

        return Mono.deferContextual(ctx -> {
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    OfferMetrics.ID.app("countMyOffers"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    (status == null)
                            ? repo.countByTenantIdAndOwnerId(tenantId, ownerId)
                            : repo.countByTenantIdAndOwnerIdAndStatus(tenantId, ownerId, status)
            );
        });
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return DEFAULT_PAGE;
        }
        if (page < 1) {
            throw new IllegalArgumentException("page must be greater than or equal to 1");
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than zero");
        }
        if (size > MAX_SIZE) {
            return MAX_SIZE;
        }
        return size;
    }

    private OfferStatusValue normalizeStatus(String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            return null;
        }
        return OfferStatusValue.of(statusCode.trim());
    }
}