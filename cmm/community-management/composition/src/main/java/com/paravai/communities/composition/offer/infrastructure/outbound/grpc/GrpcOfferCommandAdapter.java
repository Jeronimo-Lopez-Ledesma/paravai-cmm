package com.paravai.communities.composition.offer.infrastructure.outbound.grpc;

import com.paravai.communities.composition.offer.application.exception.DuplicateActiveOfferException;
import com.paravai.communities.composition.offer.application.exception.ResourceNotFoundException;
import com.paravai.communities.composition.offer.application.exception.UserNotActiveException;
import com.paravai.communities.composition.offer.port.CreateOfferCommand;
import com.paravai.communities.composition.offer.port.OfferCommandPort;
import com.paravai.communities.composition.offer.port.OfferSummary;
import com.paravai.communities.composition.offer.port.PauseOfferCommand;
import com.paravai.communities.composition.offer.port.UpdateOfferAvailabilityCommand;
import com.paravai.communities.contracts.grpc.offer.v1.CreateOfferRequest;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalCommandApiGrpc;
import com.paravai.communities.contracts.grpc.offer.v1.PauseOfferRequest;
import com.paravai.communities.contracts.grpc.offer.v1.UpdateOfferAvailabilityRequest;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Objects;

@Component
public class GrpcOfferCommandAdapter implements OfferCommandPort {

    private static final Logger log = LoggerFactory.getLogger(GrpcOfferCommandAdapter.class);

    private static final Metadata.Key<String> TRACE_ID_HEADER =
            Metadata.Key.of("traceid", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> USER_OID_HEADER =
            Metadata.Key.of("useroid", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> SOURCE_SYSTEM_HEADER =
            Metadata.Key.of("sourcesystem", Metadata.ASCII_STRING_MARSHALLER);

    private final OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub stub;

    public GrpcOfferCommandAdapter(
            OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub stub
    ) {
        this.stub = Objects.requireNonNull(stub, "stub");
    }

    @Override
    public Mono<OfferSummary> createOffer(CreateOfferCommand command) {
        Objects.requireNonNull(command, "command is required");

        return Mono.deferContextual(ctx -> {

            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info(
                    "[grpc-client][composition] Enviando metadata a offer - traceId={}, userOid={}, sourceSystem={}",
                    traceId, userOid, sourceSystem
            );

            ClientInterceptor metadataInterceptor = new ClientInterceptor() {
                @Override
                public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
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

            OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub stubWithHeaders =
                    stub.withInterceptors(metadataInterceptor);

            return Mono.fromCallable(() ->
                            stubWithHeaders.createOffer(
                                    CreateOfferRequest.newBuilder()
                                            .setTenantId(command.tenantId())
                                            .setCommunityId(command.communityId())
                                            .setResourceId(command.resourceId())
                                            .setOwnerId(command.ownerId())
                                            .setExchangeTypeCode(command.exchangeType())
                                            .setDescription(command.description() == null ? "" : command.description())
                                            .build()
                            )
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(response -> new OfferSummary(
                            response.getOfferId(),
                            response.getTenantId(),
                            response.getCommunityId(),
                            response.getResourceId(),
                            response.getOwnerId(),
                            response.getExchangeTypeCode(),
                            emptyToNull(response.getDescription()),
                            response.getStatusCode(),
                            "AVAILABLE",
                            false,
                            parseInstant(response.getCreatedAt()),
                            parseInstant(response.getUpdatedAt())
                    ))
                    .onErrorMap(this::mapToDomainException);
        });
    }

    @Override
    public Mono<OfferSummary> updateOfferAvailability(UpdateOfferAvailabilityCommand command) {
        Objects.requireNonNull(command, "command is required");

        return Mono.deferContextual(ctx -> {

            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info(
                    "[grpc-client][composition] Enviando metadata a offer - traceId={}, userOid={}, sourceSystem={}",
                    traceId, userOid, sourceSystem
            );

            ClientInterceptor metadataInterceptor = new ClientInterceptor() {
                @Override
                public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
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

            OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub stubWithHeaders =
                    stub.withInterceptors(metadataInterceptor);

            return Mono.fromCallable(() ->
                            stubWithHeaders.updateOfferAvailability(
                                    UpdateOfferAvailabilityRequest.newBuilder()
                                            .setOfferId(command.offerId())
                                            .setAvailabilityStatusCode(command.availabilityStatusCode())
                                            .build()
                            )
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(response -> new OfferSummary(
                            response.getOfferId(),
                            response.getTenantId(),
                            response.getCommunityId(),
                            response.getResourceId(),
                            response.getOwnerId(),
                            response.getExchangeTypeCode(),
                            emptyToNull(response.getDescription()),
                            response.getStatusCode(),
                            response.getAvailabilityStatusCode(),
                            response.getLocked(),
                            parseInstant(response.getCreatedAt()),
                            parseInstant(response.getUpdatedAt())
                    ))
                    .onErrorMap(this::mapToDomainException);
        });
    }

    @Override
    public Mono<OfferSummary> pauseOffer(PauseOfferCommand command) {
        Objects.requireNonNull(command, "command is required");

        return Mono.deferContextual(ctx -> {

            String traceId = RequestContext.getTraceId(ctx);
            String userOid = RequestContext.getUserOid(ctx);
            String sourceSystem = RequestContext.getSourceSystem(ctx);

            log.info(
                    "[grpc-client][composition] Enviando metadata a offer - traceId={}, userOid={}, sourceSystem={}",
                    traceId, userOid, sourceSystem
            );

            ClientInterceptor metadataInterceptor = new ClientInterceptor() {
                @Override
                public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
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

            OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub stubWithHeaders =
                    stub.withInterceptors(metadataInterceptor);

            return Mono.fromCallable(() ->
                            stubWithHeaders.pauseOffer(
                                    PauseOfferRequest.newBuilder()
                                            .setOfferId(command.offerId())
                                            .build()
                            )
                    )
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(response -> new OfferSummary(
                            response.getOfferId(),
                            response.getTenantId(),
                            response.getCommunityId(),
                            response.getResourceId(),
                            response.getOwnerId(),
                            response.getExchangeTypeCode(),
                            emptyToNull(response.getDescription()),
                            response.getStatusCode(),
                            response.getAvailabilityStatusCode(),
                            response.getLocked(),
                            parseInstant(response.getCreatedAt()),
                            parseInstant(response.getUpdatedAt())
                    ))
                    .onErrorMap(this::mapToDomainException);
        });
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private Throwable mapToDomainException(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException ex) {
            return switch (ex.getStatus().getCode()) {
                case NOT_FOUND -> new ResourceNotFoundException("unknown");
                case PERMISSION_DENIED -> new UserNotActiveException("unknown", "unknown");
                case ALREADY_EXISTS -> new DuplicateActiveOfferException();
                default -> throwable;
            };
        }

        return throwable;
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        return Instant.parse(value);
    }
}