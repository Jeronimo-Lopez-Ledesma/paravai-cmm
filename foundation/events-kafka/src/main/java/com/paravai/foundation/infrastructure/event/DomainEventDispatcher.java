package com.paravai.foundation.infrastructure.event;

import com.paravai.foundation.domain.event.DomainEvent;
import com.paravai.foundation.domain.event.DomainEventHandler;
import com.paravai.foundation.observability.metrics.OperationCtx;
import com.paravai.foundation.observability.metrics.ReactiveOperationMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;
import java.util.*;

@Slf4j
@Component
public class DomainEventDispatcher {

    private static final String METRIC_DISPATCH = "foundation.events.dispatch";
    private static final String METRIC_HANDLER  = "foundation.events.handler";

    private final Map<Class<?>, List<DomainEventHandler<?>>> handlersByType = new HashMap<>();
    private final ReactiveOperationMetrics metrics;

    public DomainEventDispatcher(
            List<DomainEventHandler<?>> allHandlers,
            ReactiveOperationMetrics metrics
    ) {
        this.metrics = Objects.requireNonNull(metrics, "metrics");

        for (DomainEventHandler<?> handler : allHandlers) {
            try {
                Class<?> targetClass = AopUtils.getTargetClass(handler);
                Class<?> eventType = resolveHandledEventType(targetClass);

                handlersByType
                        .computeIfAbsent(eventType, ignored -> new ArrayList<>())
                        .add(handler);

                log.debug("Registered DomainEventHandler [{}] for {}",
                        handler.getClass().getSimpleName(),
                        eventType.getSimpleName());
            } catch (Exception e) {
                log.warn("Could not resolve event type for handler {} — skipping registration",
                        handler.getClass().getSimpleName(), e);
            }
        }
    }

    public Mono<Void> dispatch(DomainEvent event) {
        log.debug("Pasa por DomainEventDisptacher");
        if (event == null) {
            return Mono.error(new IllegalArgumentException("DomainEvent must not be null"));
        }
        if (event.metadata() == null) {
            return Mono.error(new IllegalStateException("DomainEvent.metadata must not be null"));
        }

        final String eventType = event.getClass().getSimpleName();

        List<DomainEventHandler<?>> handlers =
                handlersByType.getOrDefault(event.getClass(), List.of());

        if (handlers.isEmpty()) {
            log.debug("No DomainEventHandler registered for event type: {}", eventType);
            return Mono.empty();
        }

        log.debug("Dispatching {} to {} handler(s)", eventType, handlers.size());

        Mono<Void> dispatchMono = Flux.fromIterable(handlers)
                .concatMap(handler -> invokeHandlerTimed(handler, event)) // preserves handler order
                .then();

        return metrics.timedMono(
                new OperationCtx(
                        METRIC_DISPATCH,
                        Map.of(
                                "eventType", eventType,
                                "handlers", Integer.toString(handlers.size())
                        )
                ),
                dispatchMono
        );
    }

    private Mono<Void> invokeHandlerTimed(DomainEventHandler<?> handler, DomainEvent event) {
        final String eventType = event.getClass().getSimpleName();
        final String handlerName = AopUtils.getTargetClass(handler).getSimpleName();

        Mono<Void> handlerMono = invokeHandler(handler, event);

        return metrics.timedMono(
                new OperationCtx(
                        METRIC_HANDLER,
                        Map.of(
                                "eventType", eventType,
                                "handler", handlerName
                        )
                ),
                handlerMono
        );
    }

    @SuppressWarnings("unchecked")
    private Mono<Void> invokeHandler(DomainEventHandler<?> handler, DomainEvent event) {
        try {
            DomainEventHandler<DomainEvent> typedHandler = (DomainEventHandler<DomainEvent>) handler;
            return typedHandler.handle(event);
        } catch (ClassCastException e) {
            return Mono.error(new IllegalStateException(
                    "Handler type mismatch for event: " + event.getClass().getSimpleName()
                            + " handler=" + handler.getClass().getSimpleName(), e));
        }
    }

    private Class<?> resolveHandledEventType(Class<?> handlerClass) {
        return Arrays.stream(handlerClass.getGenericInterfaces())
                .filter(ParameterizedType.class::isInstance)
                .map(ParameterizedType.class::cast)
                .filter(type -> type.getRawType().equals(DomainEventHandler.class))
                .map(type -> (Class<?>) type.getActualTypeArguments()[0])
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not resolve event type for handler: " + handlerClass));
    }
}
