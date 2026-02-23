package com.dekra.service.regulations.standards.api.grpc;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdRequest;
import com.dekra.service.regulations.standards.api.grpc.v1.GetStandardByIdResponse;
import com.dekra.service.regulations.standards.api.grpc.v1.StandardQueryServiceGrpc;
import com.dekra.service.regulations.standards.application.query.find.FindStandardByIdService;
import com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsRequest;
import com.dekra.service.regulations.standards.api.grpc.v1.SearchStandardsResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

@GrpcService
public class StandardQueryGrpcAdapter extends StandardQueryServiceGrpc.StandardQueryServiceImplBase {

    private final FindStandardByIdService findService;

    public StandardQueryGrpcAdapter(FindStandardByIdService findService) {
        this.findService = findService;
    }

    @Override
    public void getStandardById(GetStandardByIdRequest request,
                                StreamObserver<GetStandardByIdResponse> responseObserver) {

        IdValue id = IdValue.of(request.getStandardId());

        findService.findById(id)
                .switchIfEmpty(Mono.error(
                        Status.NOT_FOUND
                                .withDescription("Standard not found: " + request.getStandardId())
                                .asRuntimeException()
                ))
                .map(StandardGrpcMapper::toProto)
                .map(std -> GetStandardByIdResponse.newBuilder()
                        .setStandard(std)
                        .build())
                .subscribe(
                        responseObserver::onNext,
                        responseObserver::onError,
                        responseObserver::onCompleted
                );
    }

    @Override
    public void searchStandards(SearchStandardsRequest request,
                                StreamObserver<SearchStandardsResponse> obs) {

        // Validar size <= 200, page >= 1, etc.
        // Construir SearchQueryValue con filters/search/sort/pagination
        // Ejecutar search + count
        // Mapear Standard domain -> proto con StandardGrpcMapper
        // Construir response con items + total + page + size

        Mono<SearchStandardsResponse> pipeline =
                Mono.zip(
                        searchService.search(query).map(StandardGrpcMapper::toProto).collectList(),
                        searchService.count(query)
                ).map(tuple -> SearchStandardsResponse.newBuilder()
                        .addAllItems(tuple.getT1())
                        .setTotal(tuple.getT2())
                        .setPage(request.getPage())
                        .setSize(request.getSize())
                        .build()
                );

        pipeline.subscribe(obs::onNext, obs::onError, obs::onCompleted);
    }


}