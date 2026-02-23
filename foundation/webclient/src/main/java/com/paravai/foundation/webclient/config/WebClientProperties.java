package com.paravai.foundation.webclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "foundation.webclient")
public class WebClientProperties {

    private final Pool pool = new Pool();
    private final Timeout timeout = new Timeout();

    public Pool getPool() {
        return pool;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public static class Pool {
        private String name = "foundation-webclient";
        private int maxConnections = 100;
        private Duration pendingAcquireTimeout = Duration.ofMillis(500);
        private Duration maxIdleTime = Duration.ofSeconds(30);
        private Duration maxLifeTime = Duration.ofMinutes(5);

        // getters / setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

        public Duration getPendingAcquireTimeout() { return pendingAcquireTimeout; }
        public void setPendingAcquireTimeout(Duration pendingAcquireTimeout) {
            this.pendingAcquireTimeout = pendingAcquireTimeout;
        }

        public Duration getMaxIdleTime() { return maxIdleTime; }
        public void setMaxIdleTime(Duration maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }

        public Duration getMaxLifeTime() { return maxLifeTime; }
        public void setMaxLifeTime(Duration maxLifeTime) {
            this.maxLifeTime = maxLifeTime;
        }
    }

    public static class Timeout {
        private Duration connect = Duration.ofSeconds(2);
        private Duration response = Duration.ofSeconds(3);
        private Duration read = Duration.ofSeconds(4);
        private Duration write = Duration.ofSeconds(4);

        public Duration getConnect() { return connect; }
        public void setConnect(Duration connect) { this.connect = connect; }

        public Duration getResponse() { return response; }
        public void setResponse(Duration response) { this.response = response; }

        public Duration getRead() { return read; }
        public void setRead(Duration read) { this.read = read; }

        public Duration getWrite() { return write; }
        public void setWrite(Duration write) { this.write = write; }
    }
}

