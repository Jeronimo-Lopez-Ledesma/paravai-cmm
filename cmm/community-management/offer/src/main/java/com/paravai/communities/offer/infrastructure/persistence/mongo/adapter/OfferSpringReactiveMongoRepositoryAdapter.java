package com.paravai.communities.offer.infrastructure.persistence.mongo.adapter;

import com.paravai.communities.offer.application.common.OfferMetrics;
import com.paravai.communities.offer.domain.model.Offer;
import com.paravai.communities.offer.domain.repository.OfferRepository;
import com.paravai.communities.offer.domain.value.OfferStatusValue;
import com.paravai.communities.offer.infrastructure.persistence.mongo.document.OfferDocument;
import com.paravai.communities.offer.infrastructure.persistence.mongo.springdata.OfferSpringReactiveMongoRepository;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.observability.metrics.MetricsSupport;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import com.paravai.foundation.persistence.mongo.MongoReactiveEntityFilter;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

/**
 * Infrastructure adapter (Mongo + Spring Data) for OfferRepository.
 * Implements the domain port without leaking Spring/Mongo details into the domain.
 */
@Component
public class OfferSpringReactiveMongoRepositoryAdapter implements OfferRepository {

    private static final String ADAPTER_NAME = "mongo";
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final OfferSpringReactiveMongoRepository springRepo;
    private final MongoReactiveEntityFilter<Offer, OfferDocument> filter;
    private final ReactiveOperationMetrics metrics;

    public OfferSpringReactiveMongoRepositoryAdapter(
            OfferSpringReactiveMongoRepository springRepo,
            MongoReactiveEntityFilter<Offer, OfferDocument> filter,
            ReactiveOperationMetrics metrics
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "springRepo");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    @Override
    public Mono<Offer> save(Offer offer) {
        if (offer == null) {
            return Mono.error(new IllegalArgumentException("offer cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "saveOffer");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.save(OfferDocument.fromDomain(offer))
                        .map(OfferDocument::toDomain)
                        .onErrorMap(DuplicateKeyException.class, ex ->
                                new IllegalArgumentException(
                                        "Duplicate ACTIVE Offer business identity (tenantId + communityId + resourceId + status)",
                                        ex
                                )
                        )
        );
    }

    @Override
    public Mono<Offer> findById(IdValue id) {
        if (id == null) {
            return Mono.error(new IllegalArgumentException("id cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "findOfferById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findById(id.value())
                        .map(OfferDocument::toDomain)
        );
    }

    @Override
    public Mono<Offer> findActiveByTenantIdAndCommunityIdAndResourceId(
            IdValue tenantId,
            IdValue communityId,
            IdValue resourceId
    ) {
        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (communityId == null) {
            return Mono.error(new IllegalArgumentException("communityId cannot be null"));
        }
        if (resourceId == null) {
            return Mono.error(new IllegalArgumentException("resourceId cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "findActiveOfferByTenantCommunityResource");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findByTenantIdAndCommunityIdAndResourceIdAndStatusCode(
                                tenantId.value(),
                                communityId.value(),
                                resourceId.value(),
                                ACTIVE_STATUS
                        )
                        .map(OfferDocument::toDomain)
        );
    }

    @Override
    public Flux<Offer> findByTenantIdAndOwnerId(
            IdValue tenantId,
            IdValue ownerId
    ) {
        if (tenantId == null) {
            return Flux.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (ownerId == null) {
            return Flux.error(new IllegalArgumentException("ownerId cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "findOffersByTenantIdAndOwnerId");

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                springRepo.findByTenantIdAndOwnerId(
                                tenantId.value(),
                                ownerId.value()
                        )
                        .map(OfferDocument::toDomain)
        );
    }

    @Override
    public Flux<Offer> findByTenantIdAndOwnerId(
            IdValue tenantId,
            IdValue ownerId,
            int page,
            int size
    ) {
        if (tenantId == null) {
            return Flux.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (ownerId == null) {
            return Flux.error(new IllegalArgumentException("ownerId cannot be null"));
        }
        if (page < 1) {
            return Flux.error(new IllegalArgumentException("page must be greater than or equal to 1"));
        }
        if (size <= 0) {
            return Flux.error(new IllegalArgumentException("size must be greater than zero"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "findOffersByTenantIdAndOwnerIdPaged");

        long skip = (long) (page - 1) * size;

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                springRepo.findByTenantIdAndOwnerId(
                                tenantId.value(),
                                ownerId.value()
                        )
                        .skip(skip)
                        .take(size)
                        .map(OfferDocument::toDomain)
        );
    }

    @Override
    public Flux<Offer> findByTenantIdAndOwnerIdAndStatus(
            IdValue tenantId,
            IdValue ownerId,
            OfferStatusValue status,
            int page,
            int size
    ) {
        if (tenantId == null) {
            return Flux.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (ownerId == null) {
            return Flux.error(new IllegalArgumentException("ownerId cannot be null"));
        }
        if (status == null) {
            return Flux.error(new IllegalArgumentException("status cannot be null"));
        }
        if (page < 1) {
            return Flux.error(new IllegalArgumentException("page must be greater than or equal to 1"));
        }
        if (size <= 0) {
            return Flux.error(new IllegalArgumentException("size must be greater than zero"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "findOffersByTenantIdAndOwnerIdAndStatusPaged");

        long skip = (long) (page - 1) * size;

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                springRepo.findByTenantIdAndOwnerIdAndStatusCode(
                                tenantId.value(),
                                ownerId.value(),
                                status.value()
                        )
                        .skip(skip)
                        .take(size)
                        .map(OfferDocument::toDomain)
        );
    }

    @Override
    public Mono<Long> countByTenantIdAndOwnerId(
            IdValue tenantId,
            IdValue ownerId
    ) {
        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (ownerId == null) {
            return Mono.error(new IllegalArgumentException("ownerId cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "countOffersByTenantIdAndOwnerId");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.countByTenantIdAndOwnerId(
                        tenantId.value(),
                        ownerId.value()
                )
        );
    }

    @Override
    public Mono<Long> countByTenantIdAndOwnerIdAndStatus(
            IdValue tenantId,
            IdValue ownerId,
            OfferStatusValue status
    ) {
        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (ownerId == null) {
            return Mono.error(new IllegalArgumentException("ownerId cannot be null"));
        }
        if (status == null) {
            return Mono.error(new IllegalArgumentException("status cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "countOffersByTenantIdAndOwnerIdAndStatus");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.countByTenantIdAndOwnerIdAndStatusCode(
                        tenantId.value(),
                        ownerId.value(),
                        status.value()
                )
        );
    }

    @Override
    public Flux<Offer> findActiveByTenantIdAndCommunityId(
            IdValue tenantId,
            IdValue communityId,
            int page,
            int size
    ) {
        if (tenantId == null) {
            return Flux.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (communityId == null) {
            return Flux.error(new IllegalArgumentException("communityId cannot be null"));
        }
        if (page < 1) {
            return Flux.error(new IllegalArgumentException("page must be greater than or equal to 1"));
        }
        if (size <= 0) {
            return Flux.error(new IllegalArgumentException("size must be greater than zero"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(
                ADAPTER_NAME,
                "findActiveOffersByTenantIdAndCommunityIdPaged"
        );

        long skip = (long) (page - 1) * size;

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                springRepo.findByTenantIdAndCommunityIdAndStatusCode(
                                tenantId.value(),
                                communityId.value(),
                                ACTIVE_STATUS
                        )
                        .skip(skip)
                        .take(size)
                        .map(OfferDocument::toDomain)
        );
    }

    @Override
    public Mono<Long> countActiveByTenantIdAndCommunityId(
            IdValue tenantId,
            IdValue communityId
    ) {
        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (communityId == null) {
            return Mono.error(new IllegalArgumentException("communityId cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(
                ADAPTER_NAME,
                "countActiveOffersByTenantIdAndCommunityId"
        );

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.countByTenantIdAndCommunityIdAndStatusCode(
                        tenantId.value(),
                        communityId.value(),
                        ACTIVE_STATUS
                )
        );
    }




    @Override
    public Flux<Offer> search(SearchQueryValue q) {
        if (q == null) {
            return Flux.error(new IllegalArgumentException("q cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "searchOffers");

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                filter.findByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value()),
                        q.sort().isEmpty() ? Optional.empty() : Optional.of(q.sort().toString()),
                        q.page().getPage(),
                        q.page().getSize()
                )
        );
    }

    @Override
    public Mono<Long> count(SearchQueryValue q) {
        if (q == null) {
            return Mono.error(new IllegalArgumentException("q cannot be null"));
        }

        OperationCtx opCtx = OfferMetrics.ID.outbound(ADAPTER_NAME, "countOffers");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                filter.countByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value())
                )
        );
    }
}