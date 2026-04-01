package com.paravai.communities.composition.offer.infrastructure.outbound.grpc;

import com.paravai.communities.composition.offer.port.CreateOfferCommand;
import com.paravai.communities.composition.offer.port.OfferCommandPort;
import com.paravai.communities.composition.offer.port.OfferSummary;
import com.paravai.communities.contracts.grpc.offer.v1.CreateOfferRequest;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalCommandApiGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Objects;

@Component
public class GrpcOfferCommandAdapter implements OfferCommandPort {

    private final OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub stub;

    public GrpcOfferCommandAdapter(
            OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub stub
    ) {
        this.stub = Objects.requireNonNull(stub, "stub");
    }

    @Override
    public Mono<OfferSummary> createOffer(CreateOfferCommand command) {
        Objects.requireNonNull(command, "command is required");

        return Mono.fromCallable(() ->
                        stub.createOffer(
                                CreateOfferRequest.newBuilder()
                                        .setTenantId(command.tenantId())
                                        .setCommunityId(command.communityId())
                                        .setResourceId(command.resourceId())
                                        .setOwnerId(command.ownerId())
                                        .setExchangeTypeCode(command.exchangeType())
                                        .setDescription(command.description() == null ? "" : command.description())
                                        .build()
                        )
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> new OfferSummary(
                        response.getOfferId(),
                        response.getTenantId(),
                        response.getCommunityId(),
                        response.getResourceId(),
                        response.getOwnerId(),
                        response.getExchangeTypeCode(),
                        emptyToNull(response.getDescription()),
                        response.getStatusCode(),
                        parseInstant(response.getCreatedAt()),
                        parseInstant(response.getUpdatedAt())
                ))
                .onErrorMap(this::mapToDomainException);
    }

    private static String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private RuntimeException mapToDomainException(Throwable ex) {

        if (ex instanceof StatusRuntimeException sre) {
            Status.Code code = sre.getStatus().getCode();
            String msg = sre.getStatus().getDescription();

            return switch (code) {
                case ALREADY_EXISTS ->
                        new IllegalStateException(msg != null ? msg : "Offer already exists");
                case FAILED_PRECONDITION ->
                        new IllegalStateException(msg != null ? msg : "Invalid offer state");
                case INVALID_ARGUMENT ->
                        new IllegalArgumentException(msg != null ? msg : "Invalid offer request");
                case NOT_FOUND ->
                        new IllegalArgumentException(msg != null ? msg : "Related entity not found");
                default ->
                        new RuntimeException(msg != null ? msg : "Unexpected gRPC error", ex);
            };
        }

        return new RuntimeException("Unexpected error calling Offer service", ex);
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        return Instant.parse(value);
    }
}