package com.dekra.service.foundation.localization;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LocaleContextWebFilterTest {

    private final LocaleContextWebFilter filter = new LocaleContextWebFilter();

    @Test
    void shouldStoreLocaleFromAcceptLanguageHeader() {
        var request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "fr")
                .build();

        var exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = mock(WebFilterChain.class);

        // Captura el Locale desde el contexto pero devuelve Mono<Void>
        when(chain.filter(exchange)).thenReturn(
                Mono.deferContextual(ctx -> {
                    Locale locale = LocaleContext.getOrDefault(ctx);
                    assertThat(locale.getLanguage()).isEqualTo("fr");
                    return Mono.empty(); // esto sí es compatible con Mono<Void>
                })
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }


    @Test
    void shouldFallbackToEnglishIfNoHeaderPresent() {
        var request = MockServerHttpRequest.get("/test").build();
        var exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(
                Mono.deferContextual(ctx -> {
                    Locale locale = LocaleContext.getOrDefault(ctx);
                    assertThat(locale.getLanguage()).isEqualTo("en");
                    return Mono.empty();
                })
        );

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

}
