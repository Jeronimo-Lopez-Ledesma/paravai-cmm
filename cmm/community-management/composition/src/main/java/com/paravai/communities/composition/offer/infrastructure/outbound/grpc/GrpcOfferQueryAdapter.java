package com.paravai.communities.composition.offer.infrastructure.outbound.grpc;

import com.paravai.communities.composition.offer.port.OfferQueryPort;
import com.paravai.communities.composition.offer.port.OfferSummary;
import com.paravai.communities.contracts.grpc.offer.v1.ListMyOffersRequest;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalQueryApiGrpc;
import com.paravai.communities.contracts.grpc.offer.v1.ListCommunityOffersRequest;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Objects;

@Component
public class GrpcOfferQueryAdapter implements OfferQueryPort {

    private static final Logger log = LoggerFactory.getLogger(GrpcOfferQueryAdapter.class);

    private static final Metadata.Key<String> TRACE_ID_HEADER =
            Metadata.Key.of("traceid", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> USER_OID_HEADER =
            Metadata.Key.of("useroid", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> SOURCE_SYSTEM_HEADER =
            Metadata.Key.of("sourcesystem", Metadata.ASCII_STRING_MARSHALLER);

    private final OfferInternalQueryApiGrpc.OfferInternalQueryApiBlockingStub stub;

    public GrpcOfferQueryAdapter(
            OfferInternalQueryApiGrpc.OfferInternalQueryApiBlockingStub stub
    ) {
        this.stub = Objects.requireNonNull(stub, "stub");
    }

    @Override
    public Flux<OfferSummary> listMine(
            String tenantId,
            String ownerId,
            String statusCode,
            int page,
            int size
    ) {
        return Flux.deferContextual(ctx -> {

            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            ClientInterceptor interceptor = buildInterceptor(traceId, userOid, sourceSystem);

            var stubWithHeaders = stub.withInterceptors(interceptor);

            return Mono.fromCallable(() ->
                            stubWithHeaders.listMyOffers(
                                    ListMyOffersRequest.newBuilder()
                                            .setTenantId(tenantId)
                                            .setOwnerId(ownerId)
                                            .setStatusCode(statusCode == null ? "" : statusCode)
                                            .setPage(page)
                                            .setSize(size)
                                            .build()
                            )
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMapMany(response -> Flux.fromIterable(response.getItemsList()))
                    .map(this::toSummary)
                    .onErrorMap(this::mapToDomainException);
        });
    }

    @Override
    public Mono<Long> countMine(
            String tenantId,
            String ownerId,
            String statusCode
    ) {
        return Mono.deferContextual(ctx -> {

            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            ClientInterceptor interceptor = buildInterceptor(traceId, userOid, sourceSystem);

            var stubWithHeaders = stub.withInterceptors(interceptor);

            return Mono.fromCallable(() ->
                            stubWithHeaders.listMyOffers(
                                    ListMyOffersRequest.newBuilder()
                                            .setTenantId(tenantId)
                                            .setOwnerId(ownerId)
                                            .setStatusCode(statusCode == null ? "" : statusCode)
                                            .setPage(1)
                                            .setSize(1) // no importa, solo queremos total
                                            .build()
                            )
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(response -> response.getTotal())
                    .onErrorMap(this::mapToDomainException);
        });
    }

    @Override
    public Flux<OfferSummary> listCommunityOffers(
            String tenantId,
            String communityId,
            int page,
            int size
    ) {
        return Flux.deferContextual(ctx -> {

            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            ClientInterceptor interceptor = buildInterceptor(traceId, userOid, sourceSystem);

            var stubWithHeaders = stub.withInterceptors(interceptor);

            return Mono.fromCallable(() ->
                            stubWithHeaders.listCommunityOffers(
                                    ListCommunityOffersRequest.newBuilder()
                                            .setTenantId(tenantId)
                                            .setCommunityId(communityId)
                                            .setPage(page)
                                            .setSize(size)
                                            .build()
                            )
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMapMany(response -> Flux.fromIterable(response.getItemsList()))
                    .map(this::toSummary)
                    .onErrorMap(this::mapToDomainException);
        });
    }

    @Override
    public Mono<Long> countCommunityOffers(
            String tenantId,
            String communityId
    ) {
        return Mono.deferContextual(ctx -> {

            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            ClientInterceptor interceptor = buildInterceptor(traceId, userOid, sourceSystem);

            var stubWithHeaders = stub.withInterceptors(interceptor);

            return Mono.fromCallable(() ->
                            stubWithHeaders.listCommunityOffers(
                                    ListCommunityOffersRequest.newBuilder()
                                            .setTenantId(tenantId)
                                            .setCommunityId(communityId)
                                            .setPage(1)
                                            .setSize(1) // no importa, solo queremos total
                                            .build()
                            )
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(response -> response.getTotal())
                    .onErrorMap(this::mapToDomainException);
        });
    }

    private OfferSummary toSummary(
            com.paravai.communities.contracts.grpc.offer.v1.OfferItem item
    ) {
        return new OfferSummary(
                item.getOfferId(),
                item.getTenantId(),
                item.getCommunityId(),
                item.getResourceId(),
                item.getOwnerId(),
                item.getExchangeTypeCode(),
                emptyToNull(item.getDescription()),
                item.getStatusCode(),
                item.getAvailabilityStatusCode(),
                item.getLocked(),
                parseInstant(item.getCreatedAt()),
                parseInstant(item.getUpdatedAt())
        );
    }

    private ClientInterceptor buildInterceptor(String traceId, String userOid, String sourceSystem) {
        return new ClientInterceptor() {
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
                    MethodDescriptor<ReqT, RespT> method,
                    CallOptions callOptions,
                    Channel next
            ) {
                return new ForwardingClientCall.SimpleForwardingClientCall<>(
                        next.newCall(method, callOptions)
                ) {
                    @Override
                    public void start(Listener<RespT> responseListener, Metadata headers) {
                        headers.put(TRACE_ID_HEADER, traceId);
                        headers.put(USER_OID_HEADER, userOid);
                        headers.put(SOURCE_SYSTEM_HEADER, sourceSystem);
                        super.start(responseListener, headers);
                    }
                };
            }
        };
    }

    private Throwable mapToDomainException(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException ex) {
            return switch (ex.getStatus().getCode()) {
                case INVALID_ARGUMENT -> new IllegalArgumentException(ex.getMessage());
                default -> throwable;
            };
        }
        return throwable;
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        return Instant.parse(value);
    }
}