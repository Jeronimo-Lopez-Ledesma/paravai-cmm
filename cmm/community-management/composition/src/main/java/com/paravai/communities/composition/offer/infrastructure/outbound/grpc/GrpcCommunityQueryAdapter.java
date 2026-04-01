package com.paravai.communities.composition.offer.infrastructure.outbound.grpc;

import com.paravai.communities.composition.offer.port.CommunityQueryPort;
import com.paravai.communities.composition.offer.port.CommunitySummary;
import com.paravai.communities.contracts.grpc.community.v1.GetCommunityOfferPolicyRequest;
import com.paravai.communities.contracts.grpc.community.v1.CommunityInternalQueryApiGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Objects;

@Component
public class GrpcCommunityQueryAdapter implements CommunityQueryPort {

    private final CommunityInternalQueryApiGrpc.CommunityInternalQueryApiBlockingStub stub;

    public GrpcCommunityQueryAdapter(
            CommunityInternalQueryApiGrpc.CommunityInternalQueryApiBlockingStub stub
    ) {
        this.stub = Objects.requireNonNull(stub, "stub");
    }

    @Override
    public Mono<CommunitySummary> findOfferPolicy(String communityId) {
        Objects.requireNonNull(communityId, "communityId is required");

        return Mono.fromCallable(() ->
                        stub.getCommunityOfferPolicy(
                                GetCommunityOfferPolicyRequest.newBuilder()
                                        .setCommunityId(communityId)
                                        .build()
                        )
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> new CommunitySummary(
                        response.getCommunityId(),
                        new HashSet<>(response.getAllowedExchangeTypesList())
                ))
                .onErrorResume(StatusRuntimeException.class, ex -> {
                    if (Status.Code.NOT_FOUND.equals(ex.getStatus().getCode())) {
                        return Mono.empty();
                    }
                    return Mono.error(ex);
                });
    }
}