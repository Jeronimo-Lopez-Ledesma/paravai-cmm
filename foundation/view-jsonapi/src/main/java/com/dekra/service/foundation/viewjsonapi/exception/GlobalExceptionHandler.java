package com.paravai.foundation.viewjsonapi.exception;

import com.paravai.foundation.domaincore.exception.CustomException;
import com.paravai.foundation.localization.MessageService;
import com.paravai.foundation.viewjsonapi.jsonapi.DetailsError;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiErrorFactory;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiErrorResponse;
import jakarta.ws.rs.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GlobalExceptionHandler {

    private final MessageService messageService;
    @Autowired
    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleIllegalArgumentException(IllegalArgumentException e, ServerWebExchange exchange) {
        return buildTranslatedErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, null, exchange);

    }


    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleBadRequestException(BadRequestException e, ServerWebExchange exchange) {
        return buildTranslatedErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, null, exchange);
    }



    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleValidationException(WebExchangeBindException e, ServerWebExchange exchange) {

        List<DetailsError> detailsError = e.getFieldErrors().stream()
                .map(error -> {
                    String fullPath = error.getField();
                    String field = fullPath.contains(".")
                            ? fullPath.substring(fullPath.lastIndexOf('.') + 1)
                            : fullPath;
                    return JsonApiErrorFactory.of(field, error.getDefaultMessage());
                })
                .toList();

        return buildTranslatedErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, detailsError, exchange);
    }

    @ExceptionHandler(CustomException.class)
    public Mono<ResponseEntity<JsonApiErrorResponse>> handleCustomException(CustomException e, ServerWebExchange exchange) {
        return buildTranslatedErrorResponse(e.getMessageKey(), e.getCodeStatus(), null, exchange, e.getArgs());
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

        JsonApiErrorResponse response = JsonApiErrorFactory.create(status.value(), translated, list);

        return Mono.just(ResponseEntity.status(status).body(response));
    }

}
