package com.paravai.communities.composition.offer.infrastructure.outbound.grpc;

import com.paravai.communities.composition.offer.port.MembershipQueryPort;
import com.paravai.communities.composition.offer.port.MembershipSummary;
import com.paravai.communities.contracts.grpc.membership.v1.GetMembershipStatusForCommunityRequest;
import com.paravai.communities.contracts.grpc.membership.v1.MembershipInternalQueryApiGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

@Component
public class GrpcMembershipQueryAdapter implements MembershipQueryPort {

    private final MembershipInternalQueryApiGrpc.MembershipInternalQueryApiBlockingStub stub;

    public GrpcMembershipQueryAdapter(
            MembershipInternalQueryApiGrpc.MembershipInternalQueryApiBlockingStub stub
    ) {
        this.stub = Objects.requireNonNull(stub, "stub");
    }

    @Override
    public Mono<MembershipSummary> findByUserInCommunity(
            String tenantId,
            String communityId,
            String userId
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(communityId, "communityId is required");
        Objects.requireNonNull(userId, "userId is required");

        return Mono.fromCallable(() ->
                        stub.getMembershipStatusForCommunity(
                                GetMembershipStatusForCommunityRequest.newBuilder()
                                        .setTenantId(tenantId)
                                        .setCommunityId(communityId)
                                        .setUserId(userId)
                                        .build()
                        )
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> new MembershipSummary(
                        response.getMembershipId(),
                        blankToNull(response.getStatusCode()),
                        blankToNull(response.getRoleCode())
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