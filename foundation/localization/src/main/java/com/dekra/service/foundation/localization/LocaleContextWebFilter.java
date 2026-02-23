package com.dekra.service.foundation.localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
public class LocaleContextWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Locale locale = extractPreferredLocale(exchange.getRequest());
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(LocaleContext.LOCALE_KEY, locale));
    }

    private Locale extractPreferredLocale(ServerHttpRequest request) {
        return request.getHeaders()
                .getAcceptLanguageAsLocales()
                .stream()
                .findFirst()
                .orElse(Locale.ENGLISH);
    }
}
