package com.paravai.communities.membership.api.grpc;

import com.paravai.communities.contracts.grpc.membership.v1.GetMembershipStatusForCommunityRequest;
import com.paravai.communities.contracts.grpc.membership.v1.GetMembershipStatusForCommunityResponse;
import com.paravai.communities.contracts.grpc.membership.v1.MembershipInternalQueryApiGrpc;
import com.paravai.communities.membership.domain.repository.MembershipRepository;
import com.paravai.foundation.domain.value.IdValue;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import reactor.core.publisher.Mono;

import java.util.Objects;

@GrpcService
public class MembershipInternalQueryGrpcAdapter extends MembershipInternalQueryApiGrpc.MembershipInternalQueryApiImplBase {

    private final MembershipRepository membershipRepository;

    public MembershipInternalQueryGrpcAdapter(MembershipRepository membershipRepository) {
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "membershipRepository");
    }

    @Override
    public void getMembershipStatusForCommunity(
            GetMembershipStatusForCommunityRequest request,
            StreamObserver<GetMembershipStatusForCommunityResponse> responseObserver
    ) {

        IdValue tenantId = IdValue.of(request.getTenantId());
        IdValue communityId = IdValue.of(request.getCommunityId());
        IdValue userId = IdValue.of(request.getUserId());

        Mono<GetMembershipStatusForCommunityResponse> pipeline =
                membershipRepository.findByTenantIdAndCommunityIdAndUserId(
                                tenantId,
                                communityId,
                                userId
                        )
                        .switchIfEmpty(Mono.error(
                                Status.NOT_FOUND
                                        .withDescription(
                                                "Membership not found for tenantId=%s, communityId=%s, userId=%s"
                                                        .formatted(
                                                                request.getTenantId(),
                                                                request.getCommunityId(),
                                                                request.getUserId()
                                                        )
                                        )
                                        .asRuntimeException()
                        ))
                        .map(MembershipGrpcMapper::toGetMembershipStatusForCommunityResponse)
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
            String message = iae.getMessage() != null ? iae.getMessage() : "Invalid membership query";

            return Status.INVALID_ARGUMENT
                    .withDescription(message)
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription(ex.getMessage() != null ? ex.getMessage() : "Unexpected error retrieving membership")
                .withCause(ex)
                .asRuntimeException();
    }
}