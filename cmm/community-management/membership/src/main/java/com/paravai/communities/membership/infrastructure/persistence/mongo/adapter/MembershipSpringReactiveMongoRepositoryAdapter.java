package com.paravai.communities.membership.infrastructure.persistence.mongo.adapter;

import com.paravai.communities.membership.application.common.MembershipMetrics;
import com.paravai.communities.membership.domain.model.Membership;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.communities.membership.infrastructure.persistence.mongo.document.MembershipDocument;
import com.paravai.communities.membership.infrastructure.persistence.mongo.springdata.MembershipSpringReactiveMongoRepository;
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
 * Infrastructure adapter (Mongo + Spring Data) for MembershipRepository.
 * Implements the domain port without leaking Spring/Mongo details into the domain.
 */
@Component
public class MembershipSpringReactiveMongoRepositoryAdapter implements MembershipRepository {

    private static final String ADAPTER_NAME = "mongo";

    private final MembershipSpringReactiveMongoRepository springRepo;
    private final MongoReactiveEntityFilter<Membership, MembershipDocument> filter;
    private final ReactiveOperationMetrics metrics;

    public MembershipSpringReactiveMongoRepositoryAdapter(
            MembershipSpringReactiveMongoRepository springRepo,
            MongoReactiveEntityFilter<Membership, MembershipDocument> filter,
            ReactiveOperationMetrics metrics
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "springRepo");
        this.filter = Objects.requireNonNull(filter, "filter");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    @Override
    public Mono<Membership> save(Membership membership) {
        if (membership == null) {
            return Mono.error(new IllegalArgumentException("membership cannot be null"));
        }

        OperationCtx opCtx = MembershipMetrics.ID.outbound(ADAPTER_NAME, "saveMembership");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.save(MembershipDocument.fromDomain(membership))
                        .map(MembershipDocument::toDomain)
                        .onErrorMap(DuplicateKeyException.class, ex ->
                                new IllegalArgumentException(
                                        "Duplicate Membership business identity (tenantId + communityId + userId)",
                                        ex
                                )
                        )
        );
    }

    @Override
    public Mono<Membership> findById(IdValue id) {
        if (id == null) {
            return Mono.error(new IllegalArgumentException("id cannot be null"));
        }

        OperationCtx opCtx = MembershipMetrics.ID.outbound(ADAPTER_NAME, "findMembershipById");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findById(id.value())
                        .map(MembershipDocument::toDomain)
        );
    }

    @Override
    public Mono<Membership> findByTenantIdAndCommunityIdAndUserId(
            IdValue tenantId,
            IdValue communityId,
            IdValue userId
    ) {
        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (communityId == null) {
            return Mono.error(new IllegalArgumentException("communityId cannot be null"));
        }
        if (userId == null) {
            return Mono.error(new IllegalArgumentException("userId cannot be null"));
        }

        OperationCtx opCtx = MembershipMetrics.ID.outbound(ADAPTER_NAME, "findByTenantCommunityUser");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.findByTenantIdAndCommunityIdAndUserId(
                                tenantId.value(),
                                communityId.value(),
                                userId.value()
                        )
                        .map(MembershipDocument::toDomain)
        );
    }

    @Override
    public Flux<Membership> findByTenantIdAndCommunityId(
            IdValue tenantId,
            IdValue communityId
    ) {
        if (tenantId == null) {
            return Flux.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (communityId == null) {
            return Flux.error(new IllegalArgumentException("communityId cannot be null"));
        }

        OperationCtx opCtx = MembershipMetrics.ID.outbound(ADAPTER_NAME, "findByTenantCommunity");

        return MetricsSupport.timedOutboundFlux(metrics, opCtx, () ->
                springRepo.findByTenantIdAndCommunityId(
                                tenantId.value(),
                                communityId.value()
                        )
                        .map(MembershipDocument::toDomain)
        );
    }

    @Override
    public Mono<Long> countActiveAdmins(
            IdValue tenantId,
            IdValue communityId
    ) {
        if (tenantId == null) {
            return Mono.error(new IllegalArgumentException("tenantId cannot be null"));
        }
        if (communityId == null) {
            return Mono.error(new IllegalArgumentException("communityId cannot be null"));
        }

        OperationCtx opCtx = MembershipMetrics.ID.outbound(ADAPTER_NAME, "countActiveAdmins");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                springRepo.countByTenantIdAndCommunityIdAndStatusCodeAndRoleCode(
                        tenantId.value(),
                        communityId.value(),
                        "ACTIVE",
                        "ADMIN"
                )
        );
    }

    @Override
    public Flux<Membership> search(SearchQueryValue q) {
        if (q == null) {
            return Flux.error(new IllegalArgumentException("q cannot be null"));
        }

        OperationCtx opCtx = MembershipMetrics.ID.outbound(ADAPTER_NAME, "searchMemberships");

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

        OperationCtx opCtx = MembershipMetrics.ID.outbound(ADAPTER_NAME, "countMemberships");

        return MetricsSupport.timedOutboundMono(metrics, opCtx, () ->
                filter.countByFilters(
                        q.filters().values(),
                        q.search().isEmpty() ? Optional.empty() : Optional.of(q.search().value())
                )
        );
    }
}