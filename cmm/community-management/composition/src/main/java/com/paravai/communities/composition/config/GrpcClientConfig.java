package com.paravai.communities.composition.config;

import com.paravai.communities.contracts.grpc.community.v1.CommunityInternalQueryApiGrpc;
import com.paravai.communities.contracts.grpc.membership.v1.MembershipInternalQueryApiGrpc;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalCommandApiGrpc;
import com.paravai.communities.contracts.grpc.resource.v1.ResourceInternalQueryApiGrpc;
import com.paravai.communities.contracts.grpc.offer.v1.OfferInternalQueryApiGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GrpcClientsProperties.class)
public class GrpcClientConfig {

    @Bean("resourceManagedChannel")
    public ManagedChannel resourceManagedChannel(GrpcClientsProperties properties) {
        return ManagedChannelBuilder
                .forAddress(
                        properties.getResource().getHost(),
                        properties.getResource().getPort()
                )
                .usePlaintext()
                .build();
    }

    @Bean("membershipManagedChannel")
    public ManagedChannel membershipManagedChannel(GrpcClientsProperties properties) {
        return ManagedChannelBuilder
                .forAddress(
                        properties.getMembership().getHost(),
                        properties.getMembership().getPort()
                )
                .usePlaintext()
                .build();
    }

    @Bean("communityManagedChannel")
    public ManagedChannel communityManagedChannel(GrpcClientsProperties properties) {
        return ManagedChannelBuilder
                .forAddress(
                        properties.getCommunity().getHost(),
                        properties.getCommunity().getPort()
                )
                .usePlaintext()
                .build();
    }

    @Bean("offerManagedChannel")
    public ManagedChannel offerManagedChannel(GrpcClientsProperties properties) {
        return ManagedChannelBuilder
                .forAddress(
                        properties.getOffer().getHost(),
                        properties.getOffer().getPort()
                )
                .usePlaintext()
                .build();
    }

    @Bean
    public ResourceInternalQueryApiGrpc.ResourceInternalQueryApiBlockingStub resourceInternalQueryApiBlockingStub(
            @Qualifier("resourceManagedChannel") ManagedChannel managedChannel
    ) {
        return ResourceInternalQueryApiGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public MembershipInternalQueryApiGrpc.MembershipInternalQueryApiBlockingStub membershipInternalQueryApiBlockingStub(
            @Qualifier("membershipManagedChannel") ManagedChannel managedChannel
    ) {
        return MembershipInternalQueryApiGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public CommunityInternalQueryApiGrpc.CommunityInternalQueryApiBlockingStub communityInternalQueryApiBlockingStub(
            @Qualifier("communityManagedChannel") ManagedChannel managedChannel
    ) {
        return CommunityInternalQueryApiGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public OfferInternalCommandApiGrpc.OfferInternalCommandApiBlockingStub offerInternalCommandApiBlockingStub(
            @Qualifier("offerManagedChannel") ManagedChannel managedChannel
    ) {
        return OfferInternalCommandApiGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public OfferInternalQueryApiGrpc.OfferInternalQueryApiBlockingStub offerInternalQueryApiBlockingStub(
            @Qualifier("offerManagedChannel") ManagedChannel managedChannel
    ) {
        return OfferInternalQueryApiGrpc.newBlockingStub(managedChannel);
    }
}