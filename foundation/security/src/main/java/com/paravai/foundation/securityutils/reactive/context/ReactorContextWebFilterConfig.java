package com.paravai.foundation.securityutils.reactive.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
public class ReactorContextWebFilterConfig {

    private static final Logger log = LoggerFactory.getLogger(ReactorContextWebFilterConfig.class);

    @Bean
    public WebFilter reactorRequestContextFilter() {
        return (exchange, chain) -> {
            String traceId = exchange.getRequest().getHeaders().getFirst("x-trace-id");
            String userOid = exchange.getRequest().getHeaders().getFirst("x-user-oid");

            log.info("[{}][{}] Context traceId={} userOid={}", traceId, userOid, traceId, userOid);

            return chain.filter(exchange)
                    .contextWrite(ctx -> ctx
                            .put(RequestContext.TRACE_ID_KEY, traceId != null ? traceId : "missing-trace-id")
                            .put(RequestContext.USER_OID_KEY, userOid != null ? userOid : "anonymous"));
        };
    }
}