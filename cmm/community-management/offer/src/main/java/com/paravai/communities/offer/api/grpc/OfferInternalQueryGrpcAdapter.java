package com.paravai.communities.offer.api.grpc;

import com.paravai.communities.contracts.grpc.offer.v1.ListMyOffersRequest;
import com.paravai.communities.contracts.grpc.offer.v1.ListMyOffersResponse;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalQueryApiGrpc;
import com.paravai.communities.offer.application.query.listmy.ListMyOffersService;
import com.paravai.foundation.domain.value.IdValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.util.Objects;

@GrpcService
public class OfferInternalQueryGrpcAdapter
        extends OfferInternalQueryApiGrpc.OfferInternalQueryApiImplBase {

    private final ListMyOffersService listMyOffersService;

    public OfferInternalQueryGrpcAdapter(ListMyOffersService listMyOffersService) {
        this.listMyOffersService = Objects.requireNonNull(listMyOffersService, "listMyOffersService");
    }

    @Override
    public void listMyOffers(
            ListMyOffersRequest request,
            StreamObserver<ListMyOffersResponse> responseObserver
    ) {

        IdValue tenantId = IdValue.of(request.getTenantId());
        IdValue ownerId = IdValue.of(request.getOwnerId());

        String statusCode = request.getStatusCode();
        int page = request.getPage();
        int size = request.getSize();

        Mono<ListMyOffersResponse> pipeline =
                Mono.zip(
                                listMyOffersService.list(tenantId, ownerId, statusCode, page, size).collectList(),
                                listMyOffersService.count(tenantId, ownerId, statusCode)
                        )
                        .map(tuple -> {
                            var offers = tuple.getT1();
                            var total = tuple.getT2();

                            return ListMyOffersResponse.newBuilder()
                                    .addAllItems(
                                            offers.stream()
                                                    .map(OfferGrpcMapper::toOfferItem)
                                                    .toList()
                                    )
                                    .setTotal(total)
                                    .setPage(page)
                                    .setSize(size)
                                    .build();
                        })
                        .onErrorMap(this::mapToGrpcException);

        pipeline.subscribe(
                responseObserver::onNext,
                responseObserver::onError,
                responseObserver::onCompleted
        );
    }

    private Throwable mapToGrpcException(Throwable ex) {

        if (ex instanceof io.grpc.StatusRuntimeException) {
            return ex;
        }

        if (ex instanceof IllegalArgumentException iae) {
            String message = iae.getMessage() != null ? iae.getMessage() : "Invalid offer query";

            return Status.INVALID_ARGUMENT
                    .withDescription(message)
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription(ex.getMessage() != null ? ex.getMessage() : "Unexpected error retrieving offers")
                .withCause(ex)
                .asRuntimeException();
    }
}