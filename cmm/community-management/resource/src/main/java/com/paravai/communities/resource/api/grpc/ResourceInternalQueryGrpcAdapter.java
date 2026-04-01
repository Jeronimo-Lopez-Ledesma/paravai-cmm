package com.paravai.communities.resource.api.grpc;

import com.paravai.communities.contracts.grpc.resource.v1.GetOwnedResourceRequest;
import com.paravai.communities.contracts.grpc.resource.v1.GetOwnedResourceResponse;
import com.paravai.communities.contracts.grpc.resource.v1.ResourceInternalQueryApiGrpc;
import com.paravai.communities.resource.application.query.get.GetResourceService;
import com.paravai.foundation.domain.value.IdValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.util.Objects;

@GrpcService
public class ResourceInternalQueryGrpcAdapter extends ResourceInternalQueryApiGrpc.ResourceInternalQueryApiImplBase {

    private final GetResourceService getResourceService;

    public ResourceInternalQueryGrpcAdapter(GetResourceService getResourceService) {
        this.getResourceService = Objects.requireNonNull(getResourceService, "getResourceService");
    }

    @Override
    public void getOwnedResource(GetOwnedResourceRequest request,
                                 StreamObserver<GetOwnedResourceResponse> responseObserver) {

        IdValue resourceId = IdValue.of(request.getResourceId());
        IdValue ownerId = IdValue.of(request.getOwnerId());

        Mono<GetOwnedResourceResponse> pipeline =
                getResourceService.get(resourceId, ownerId)
                        .map(result -> ResourceGrpcMapper.toGetOwnedResourceResponse(result.resource()))
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
            String message = iae.getMessage() != null ? iae.getMessage() : "Invalid resource query";

            if (message.toLowerCase().contains("not found")) {
                return Status.NOT_FOUND
                        .withDescription(message)
                        .asRuntimeException();
            }

            return Status.INVALID_ARGUMENT
                    .withDescription(message)
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription(ex.getMessage() != null ? ex.getMessage() : "Unexpected error retrieving resource")
                .withCause(ex)
                .asRuntimeException();
    }
}