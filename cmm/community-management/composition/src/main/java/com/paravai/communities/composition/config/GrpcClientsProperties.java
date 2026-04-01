package com.paravai.communities.composition.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paravai.grpc")
public class GrpcClientsProperties {

    private final Target resource = new Target();
    private final Target membership = new Target();
    private final Target community = new Target();
    private final Target offer = new Target();

    public Target getResource() {
        return resource;
    }
    public Target getMembership() {
        return membership;
    }
    public Target getCommunity() {
        return community;
    }
    public Target getOffer() {
        return offer;
    }

    public static class Target {
        private String host = "localhost";
        private int port = 9092;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}