package com.paravai.communities.community.api.grpc;

import com.paravai.communities.contracts.grpc.community.v1.GetCommunityOfferPolicyRequest;
import com.paravai.communities.contracts.grpc.community.v1.GetCommunityOfferPolicyResponse;
import com.paravai.communities.contracts.grpc.community.v1.CommunityInternalQueryApiGrpc;
import com.paravai.communities.community.domain.repository.CommunityRepository;
import com.paravai.foundation.domain.value.IdValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.util.Objects;

@GrpcService
public class CommunityInternalQueryGrpcAdapter extends CommunityInternalQueryApiGrpc.CommunityInternalQueryApiImplBase {

    private final CommunityRepository communityRepository;

    public CommunityInternalQueryGrpcAdapter(CommunityRepository communityRepository) {
        this.communityRepository = Objects.requireNonNull(communityRepository, "communityRepository");
    }

    @Override
    public void getCommunityOfferPolicy(
            GetCommunityOfferPolicyRequest request,
            StreamObserver<GetCommunityOfferPolicyResponse> responseObserver
    ) {

        IdValue communityId = IdValue.of(request.getCommunityId());

        Mono<GetCommunityOfferPolicyResponse> pipeline =
                communityRepository.findById(communityId)
                        .switchIfEmpty(Mono.error(
                                Status.NOT_FOUND
                                        .withDescription("Community not found: " + request.getCommunityId())
                                        .asRuntimeException()
                        ))
                        .map(CommunityGrpcMapper::toGetCommunityOfferPolicyResponse)
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
            String message = iae.getMessage() != null ? iae.getMessage() : "Invalid community query";

            return Status.INVALID_ARGUMENT
                    .withDescription(message)
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription(ex.getMessage() != null ? ex.getMessage() : "Unexpected error retrieving community")
                .withCause(ex)
                .asRuntimeException();
    }
}