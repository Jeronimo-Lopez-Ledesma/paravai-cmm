package com.paravai.communities.offer.application.query.listcommunity;

import com.paravai.communities.offer.application.common.OfferMetrics;
import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.domain.repository.OfferRepository;
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
public class ListCommunityOffersService {

    private static final Logger log = LoggerFactory.getLogger(ListCommunityOffersService.class);

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final OfferRepository repo;
    private final ReactiveOperationMetrics metrics;

    public ListCommunityOffersService(
            OfferRepository repo,
            ReactiveOperationMetrics metrics
    ) {
        this.repo = Objects.requireNonNull(repo, "repo");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public Flux<Offer> list(
            IdValue tenantId,
            IdValue communityId,
            Integer page,
            Integer size
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");

        int effectivePage = normalizePage(page);
        int effectiveSize = normalizeSize(size);

        return Flux.deferContextual(ctx -> {
            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    OfferMetrics.ID.app("listCommunityOffers"),
                    sourceSystem
            );

            return MetricsSupport.timedFlux(metrics, opCtx, () ->
                    repo.findActiveByTenantIdAndCommunityId(
                                    tenantId,
                                    communityId,
                                    effectivePage,
                                    effectiveSize
                            )
                            .doOnError(ex -> log.error(
                                    "[{}][{}] Failed to list ACTIVE offers for community {}",
                                    traceId,
                                    userOid,
                                    communityId.value(),
                                    ex
                            ))
            );
        });
    }

    public Mono<Long> count(
            IdValue tenantId,
            IdValue communityId
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");

        return Mono.deferContextual(ctx -> {
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            OperationCtx opCtx = MetricsSupport.withSourceSystem(
                    OfferMetrics.ID.app("countCommunityOffers"),
                    sourceSystem
            );

            return MetricsSupport.timedMono(metrics, opCtx, () ->
                    repo.countActiveByTenantIdAndCommunityId(
                            tenantId,
                            communityId
                    )
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
}