package com.paravai.foundation.webclient.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@EnableConfigurationProperties(WebClientProperties.class)
public class WebClientAutoConfiguration {

    @Bean
    public ConnectionProvider webClientConnectionProvider(WebClientProperties properties) {

        var pool = properties.getPool();

        return ConnectionProvider.builder(pool.getName())
                .maxConnections(pool.getMaxConnections())
                .pendingAcquireTimeout(pool.getPendingAcquireTimeout())
                .maxIdleTime(pool.getMaxIdleTime())
                .maxLifeTime(pool.getMaxLifeTime())
                .metrics(true)
                .build();
    }

    @Bean
    public HttpClient foundationHttpClient(
            ConnectionProvider provider,
            WebClientProperties properties
    ) {
        var timeout = properties.getTimeout();

        return HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) timeout.getConnect().toMillis())
                .responseTimeout(timeout.getResponse())
                .doOnConnected(conn -> conn
                        .addHandlerLast(
                                new ReadTimeoutHandler(timeout.getRead().toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(
                                new WriteTimeoutHandler(timeout.getWrite().toMillis(), TimeUnit.MILLISECONDS))
                );
    }

    @Bean
    public WebClient.Builder foundationWebClientBuilder(HttpClient foundationHttpClient) {

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(foundationHttpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build());
    }
}
