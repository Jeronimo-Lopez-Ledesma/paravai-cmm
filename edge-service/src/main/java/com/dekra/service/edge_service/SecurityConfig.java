package com.paravai.edge_service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         RedisAuthenticationManager authManager,
                                                         JwtServerAuthenticationConverter converter) {

        AuthenticationWebFilter authWebFilter = new AuthenticationWebFilter(authManager);
        authWebFilter.setServerAuthenticationConverter(converter);
        authWebFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        authWebFilter.setRequiresAuthenticationMatcher(
                ServerWebExchangeMatchers.pathMatchers("/actuator/prometheus")
        );

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**", "/public/**").permitAll()
                        .pathMatchers("/actuator/prometheus").authenticated()
                        .anyExchange().permitAll() // o .authenticated() ?? Más complejo usando SpringSecurity
                )
                .addFilterAt(authWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}