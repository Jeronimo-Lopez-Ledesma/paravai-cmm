package com.paravai.foundation.viewjsonapi.exception;

import com.paravai.foundation.domain.exception.CustomException;
import com.paravai.foundation.localization.MessageService;
import com.paravai.foundation.viewjsonapi.jsonapi.DetailsError;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiErrorFactory;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiErrorResponse;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@RestControllerAdvice
@Order(-2)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageService messageService;

    @Autowired
    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException e,
            ServerWebExchange exchange
    ) {
        log.warn("IllegalArgumentException handled: {}", e.getMessage());
        return buildTranslatedErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, null, exchange);
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleBadRequestException(
            BadRequestException e,
            ServerWebExchange exchange
    ) {
        log.warn("BadRequestException handled: {}", e.getMessage());
        return buildTranslatedErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, null, exchange);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleValidationException(
            WebExchangeBindException e,
            ServerWebExchange exchange
    ) {
        List<DetailsError> detailsError = e.getFieldErrors().stream()
                .map(error -> {
                    String fullPath = error.getField();
                    String field = fullPath.contains(".")
                            ? fullPath.substring(fullPath.lastIndexOf('.') + 1)
                            : fullPath;
                    return JsonApiErrorFactory.of(field, error.getDefaultMessage());
                })
                .toList();

        log.warn("WebExchangeBindException handled: {}", e.getMessage());

        return buildTranslatedErrorResponse(
                "error.request.validation",
                HttpStatus.BAD_REQUEST,
                detailsError,
                exchange
        );
    }

    @ExceptionHandler(CustomException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleCustomException(
            CustomException e,
            ServerWebExchange exchange
    ) {
        log.warn("CustomException handled: key={}, status={}", e.getMessageKey(), e.getCodeStatus());
        return buildTranslatedErrorResponse(
                e.getMessageKey(),
                e.getCodeStatus(),
                null,
                exchange,
                e.getArgs()
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleUnexpectedException(
            Exception e,
            ServerWebExchange exchange
    ) {
        log.error("Unexpected exception handled", e);
        return buildTranslatedErrorResponse(
                "error.internalServerError",
                HttpStatus.INTERNAL_SERVER_ERROR,
                null,
                exchange
        );
    }

    private Mono<ResponseEntity<JsonApiErrorResponse>> buildTranslatedErrorResponse(
            String messageKey,
            HttpStatus status,
            List<DetailsError> list,
            ServerWebExchange exchange,
            Object... args
    ) {
        Locale locale = exchange.getRequest().getHeaders()
                .getAcceptLanguageAsLocales()
                .stream()
                .findFirst()
                .orElse(Locale.ENGLISH);

        String translated = messageService.get(messageKey, args, messageKey, locale);

        JsonApiErrorResponse response = JsonApiErrorFactory.create(
                status.value(),
                translated,
                list
        );

        return Mono.just(ResponseEntity.status(status).body(response));
    }
}