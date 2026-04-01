package com.paravai.communities.composition.offer.infrastructure.outbound.grpc;

import com.paravai.communities.composition.offer.port.ResourceQueryPort;
import com.paravai.communities.composition.offer.port.ResourceSummary;
import com.paravai.communities.contracts.grpc.resource.v1.GetOwnedResourceRequest;
import com.paravai.communities.contracts.grpc.resource.v1.ResourceInternalQueryApiGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class GrpcResourceQueryAdapter implements ResourceQueryPort {

    private final ResourceInternalQueryApiGrpc.ResourceInternalQueryApiBlockingStub stub;

    public GrpcResourceQueryAdapter(
            ResourceInternalQueryApiGrpc.ResourceInternalQueryApiBlockingStub stub
    ) {
        this.stub = Objects.requireNonNull(stub, "stub");
    }

    @Override
    public Mono<ResourceSummary> findOwnedById(String resourceId, String ownerId) {
        Objects.requireNonNull(resourceId, "resourceId is required");
        Objects.requireNonNull(ownerId, "ownerId is required");

        return Mono.fromCallable(() ->
                        stub.getOwnedResource(
                                GetOwnedResourceRequest.newBuilder()
                                        .setResourceId(resourceId)
                                        .setOwnerId(ownerId)
                                        .build()
                        )
                )
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .map(response -> new ResourceSummary(
                        response.getResourceId(),
                        response.getOwnerId(),
                        blankToNull(response.getTitle())
                ))
                .onErrorResume(StatusRuntimeException.class, ex -> {
                    if (Status.Code.NOT_FOUND.equals(ex.getStatus().getCode())) {
                        return Mono.empty();
                    }
                    return Mono.error(ex);
                });
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}