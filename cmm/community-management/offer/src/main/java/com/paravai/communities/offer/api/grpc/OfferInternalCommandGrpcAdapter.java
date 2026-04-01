package com.paravai.communities.offer.api.grpc;

import com.paravai.communities.contracts.grpc.offer.v1.CreateOfferRequest;
import com.paravai.communities.contracts.grpc.offer.v1.CreateOfferResponse;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalCommandApiGrpc;
import com.paravai.communities.offer.application.command.create.CreateOfferService;
import com.paravai.foundation.domain.value.IdValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.util.Objects;

@GrpcService
public class OfferInternalCommandGrpcAdapter extends OfferInternalCommandApiGrpc.OfferInternalCommandApiImplBase {

    private final CreateOfferService createOfferService;

    public OfferInternalCommandGrpcAdapter(CreateOfferService createOfferService) {
        this.createOfferService = Objects.requireNonNull(createOfferService, "createOfferService");
    }

    @Override
    public void createOffer(CreateOfferRequest request,
                            StreamObserver<CreateOfferResponse> responseObserver) {

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
                        .onErrorMap(this::mapToGrpcException);

        pipeline.subscribe(
                responseObserver::onNext,
                responseObserver::onError,
                responseObserver::onCompleted
        );
    }

    private Throwable mapToGrpcException(Throwable ex) {

        if (ex instanceof IllegalArgumentException iae) {
            String message = iae.getMessage() != null ? iae.getMessage() : "Invalid offer request";

            if (message.contains("already exists")) {
                return Status.ALREADY_EXISTS
                        .withDescription(message)
                        .asRuntimeException();
            }

            return Status.INVALID_ARGUMENT
                    .withDescription(message)
                    .asRuntimeException();
        }

        if (ex instanceof IllegalStateException ise) {
            String message = ise.getMessage() != null ? ise.getMessage() : "Invalid offer state";

            return Status.FAILED_PRECONDITION
                    .withDescription(message)
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription(ex.getMessage() != null ? ex.getMessage() : "Unexpected error creating offer")
                .withCause(ex)
                .asRuntimeException();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}