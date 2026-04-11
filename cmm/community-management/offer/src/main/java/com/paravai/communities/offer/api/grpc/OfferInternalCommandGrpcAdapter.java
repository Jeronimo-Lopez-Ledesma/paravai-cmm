package com.paravai.communities.offer.api.grpc;

import com.paravai.communities.contracts.grpc.offer.v1.CreateOfferRequest;
import com.paravai.communities.contracts.grpc.offer.v1.CreateOfferResponse;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalCommandApiGrpc;
import com.paravai.communities.contracts.grpc.offer.v1.PauseOfferRequest;
import com.paravai.communities.contracts.grpc.offer.v1.PauseOfferResponse;
import com.paravai.communities.contracts.grpc.offer.v1.UpdateOfferAvailabilityRequest;
import com.paravai.communities.contracts.grpc.offer.v1.UpdateOfferAvailabilityResponse;
import com.paravai.communities.contracts.grpc.offer.v1.WithdrawOfferRequest;
import com.paravai.communities.contracts.grpc.offer.v1.WithdrawOfferResponse;
import com.paravai.communities.offer.application.command.create.CreateOfferService;
import com.paravai.communities.offer.application.command.pause.PauseOfferService;
import com.paravai.communities.offer.application.command.updateavailability.UpdateOfferAvailabilityService;
import com.paravai.communities.offer.application.command.withdraw.WithdrawOfferService;
import com.paravai.foundation.domain.exception.CustomException;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.util.Objects;

@GrpcService
public class OfferInternalCommandGrpcAdapter extends OfferInternalCommandApiGrpc.OfferInternalCommandApiImplBase {

    private static final String FALLBACK_TRACE_ID = "missing-trace-id";
    private static final String FALLBACK_USER_OID = "anonymous";
    private static final String FALLBACK_SOURCE_SYSTEM = "unknown";

    /**
     * These keys must match the ones populated by your gRPC server interceptor.
     */
    public static final Context.Key<String> TRACE_ID_CTX_KEY = Context.key("traceId");
    public static final Context.Key<String> USER_OID_CTX_KEY = Context.key("userOid");
    public static final Context.Key<String> SOURCE_SYSTEM_CTX_KEY = Context.key("sourceSystem");

    private final CreateOfferService createOfferService;
    private final UpdateOfferAvailabilityService updateOfferAvailabilityService;
    private final PauseOfferService pauseOfferService;
    private final WithdrawOfferService withdrawOfferService;

    public OfferInternalCommandGrpcAdapter(CreateOfferService createOfferService,
                                           UpdateOfferAvailabilityService updateOfferAvailabilityService,
                                           PauseOfferService pauseOfferService,
                                           WithdrawOfferService withdrawOfferService) {
        this.createOfferService = Objects.requireNonNull(createOfferService, "createOfferService");
        this.updateOfferAvailabilityService = Objects.requireNonNull(updateOfferAvailabilityService, "updateOfferAvailabilityService");
        this.pauseOfferService = Objects.requireNonNull(pauseOfferService, "pauseOfferService");
        this.withdrawOfferService = Objects.requireNonNull(withdrawOfferService, "withdrawOfferService");
    }

    @Override
    public void createOffer(CreateOfferRequest request,
                            StreamObserver<CreateOfferResponse> responseObserver) {

        String traceId = defaultIfBlank(TRACE_ID_CTX_KEY.get(), FALLBACK_TRACE_ID);
        String userOid = defaultIfBlank(USER_OID_CTX_KEY.get(), FALLBACK_USER_OID);
        String sourceSystem = defaultIfBlank(SOURCE_SYSTEM_CTX_KEY.get(), FALLBACK_SOURCE_SYSTEM);

        org.slf4j.LoggerFactory.getLogger(OfferInternalCommandGrpcAdapter.class)
                .info("[grpc-adapter][offer] Context values resolved - traceId={}, userOid={}, sourceSystem={}",
                        traceId, userOid, sourceSystem);

        Mono<CreateOfferResponse> pipeline =
                createOfferService.create(
                                IdValue.of(request.getTenantId()),
                                IdValue.of(request.getCommunityId()),
                                IdValue.of(request.getResourceId()),
                                IdValue.of(request.getOwnerId()),
                                request.getExchangeTypeCode(),
                                blankToNull(request.getDescription())
                        )
                        .map(result -> OfferGrpcMapper.toCreateOfferResponse(result.offer()))
                        .contextWrite(ctx -> ctx
                                .put(RequestContext.TRACE_ID_KEY, traceId)
                                .put(RequestContext.USER_OID_KEY, userOid)
                                .put(RequestContext.SOURCE_SYSTEM_KEY, sourceSystem)
                        )
                        .onErrorMap(this::mapToGrpcException);

        pipeline.subscribe(
                responseObserver::onNext,
                responseObserver::onError,
                responseObserver::onCompleted
        );
    }

    @Override
    public void updateOfferAvailability(UpdateOfferAvailabilityRequest request,
                                        StreamObserver<UpdateOfferAvailabilityResponse> responseObserver) {

        String traceId = defaultIfBlank(TRACE_ID_CTX_KEY.get(), FALLBACK_TRACE_ID);
        String userOid = defaultIfBlank(USER_OID_CTX_KEY.get(), FALLBACK_USER_OID);
        String sourceSystem = defaultIfBlank(SOURCE_SYSTEM_CTX_KEY.get(), FALLBACK_SOURCE_SYSTEM);

        org.slf4j.LoggerFactory.getLogger(OfferInternalCommandGrpcAdapter.class)
                .info("[grpc-adapter][offer] Context values resolved - traceId={}, userOid={}, sourceSystem={}",
                        traceId, userOid, sourceSystem);

        Mono<UpdateOfferAvailabilityResponse> pipeline =
                updateOfferAvailabilityService.updateAvailability(
                                IdValue.of(request.getOfferId()),
                                IdValue.of(userOid),
                                request.getAvailabilityStatusCode()
                        )
                        .map(result -> OfferGrpcMapper.toUpdateOfferAvailabilityResponse(result.offer()))
                        .contextWrite(ctx -> ctx
                                .put(RequestContext.TRACE_ID_KEY, traceId)
                                .put(RequestContext.USER_OID_KEY, userOid)
                                .put(RequestContext.SOURCE_SYSTEM_KEY, sourceSystem)
                        )
                        .onErrorMap(this::mapToGrpcException);

        pipeline.subscribe(
                responseObserver::onNext,
                responseObserver::onError,
                responseObserver::onCompleted
        );
    }

    @Override
    public void pauseOffer(PauseOfferRequest request,
                           StreamObserver<PauseOfferResponse> responseObserver) {

        String traceId = defaultIfBlank(TRACE_ID_CTX_KEY.get(), FALLBACK_TRACE_ID);
        String userOid = defaultIfBlank(USER_OID_CTX_KEY.get(), FALLBACK_USER_OID);
        String sourceSystem = defaultIfBlank(SOURCE_SYSTEM_CTX_KEY.get(), FALLBACK_SOURCE_SYSTEM);

        org.slf4j.LoggerFactory.getLogger(OfferInternalCommandGrpcAdapter.class)
                .info("[grpc-adapter][offer] Context values resolved - traceId={}, userOid={}, sourceSystem={}",
                        traceId, userOid, sourceSystem);

        Mono<PauseOfferResponse> pipeline =
                pauseOfferService.pause(
                                IdValue.of(request.getOfferId()),
                                IdValue.of(userOid)
                        )
                        .map(result -> OfferGrpcMapper.toPauseOfferResponse(result.offer()))
                        .contextWrite(ctx -> ctx
                                .put(RequestContext.TRACE_ID_KEY, traceId)
                                .put(RequestContext.USER_OID_KEY, userOid)
                                .put(RequestContext.SOURCE_SYSTEM_KEY, sourceSystem)
                        )
                        .onErrorMap(this::mapToGrpcException);

        pipeline.subscribe(
                responseObserver::onNext,
                responseObserver::onError,
                responseObserver::onCompleted
        );
    }

    @Override
    public void withdrawOffer(WithdrawOfferRequest request,
                              StreamObserver<WithdrawOfferResponse> responseObserver) {

        String traceId = defaultIfBlank(TRACE_ID_CTX_KEY.get(), FALLBACK_TRACE_ID);
        String userOid = defaultIfBlank(USER_OID_CTX_KEY.get(), FALLBACK_USER_OID);
        String sourceSystem = defaultIfBlank(SOURCE_SYSTEM_CTX_KEY.get(), FALLBACK_SOURCE_SYSTEM);

        org.slf4j.LoggerFactory.getLogger(OfferInternalCommandGrpcAdapter.class)
                .info("[grpc-adapter][offer] Context values resolved - traceId={}, userOid={}, sourceSystem={}",
                        traceId, userOid, sourceSystem);

        Mono<WithdrawOfferResponse> pipeline =
                withdrawOfferService.withdraw(
                                IdValue.of(request.getOfferId()),
                                IdValue.of(userOid)
                        )
                        .map(result -> OfferGrpcMapper.toWithdrawOfferResponse(result.offer()))
                        .contextWrite(ctx -> ctx
                                .put(RequestContext.TRACE_ID_KEY, traceId)
                                .put(RequestContext.USER_OID_KEY, userOid)
                                .put(RequestContext.SOURCE_SYSTEM_KEY, sourceSystem)
                        )
                        .onErrorMap(this::mapToGrpcException);

        pipeline.subscribe(
                responseObserver::onNext,
                responseObserver::onError,
                responseObserver::onCompleted
        );
    }

    private Throwable mapToGrpcException(Throwable ex) {

        if (ex instanceof CustomException ce) {
            String message = ce.getMessageKey();

            return switch (ce.getCodeStatus()) {
                case BAD_REQUEST -> Status.INVALID_ARGUMENT.withDescription(message).asRuntimeException();
                case UNAUTHORIZED -> Status.UNAUTHENTICATED.withDescription(message).asRuntimeException();
                case FORBIDDEN -> Status.PERMISSION_DENIED.withDescription(message).asRuntimeException();
                case NOT_FOUND -> Status.NOT_FOUND.withDescription(message).asRuntimeException();
                case CONFLICT -> Status.ALREADY_EXISTS.withDescription(message).asRuntimeException();
                default -> Status.INTERNAL.withDescription(message).withCause(ex).asRuntimeException();
            };
        }

        if (ex instanceof IllegalArgumentException iae) {
            String message = iae.getMessage() != null ? iae.getMessage() : "Invalid offer request";
            return Status.INVALID_ARGUMENT.withDescription(message).asRuntimeException();
        }

        if (ex instanceof IllegalStateException ise) {
            String message = ise.getMessage() != null ? ise.getMessage() : "Invalid offer state";
            return Status.FAILED_PRECONDITION.withDescription(message).asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription(ex.getMessage() != null ? ex.getMessage() : "Unexpected error processing offer command")
                .withCause(ex)
                .asRuntimeException();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}