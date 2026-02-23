package com.paravai.foundation.persistence.mongo.resilience;

import com.paravai.foundation.persistence.mongo.resilience.metrics.MongoResilienceMetrics;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RetryBudgetService {

    private static final int DEFAULT_BUDGET = 50; // retries/min por operación Mongo
    private static final int RESET_INTERVAL_SECONDS = 60;

    private final Map<String, AtomicInteger> budgets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final MongoResilienceMetrics metrics;

    public RetryBudgetService(MongoResilienceMetrics metrics) {
        this.metrics = metrics;

        // Hilo dedicado, friendly para logs y Kubernetes
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                new CustomizableThreadFactory("retry-budget-resetter-")
        );

        scheduler.scheduleAtFixedRate(
                this::resetBudgets,
                RESET_INTERVAL_SECONDS,
                RESET_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
    }

    private void resetBudgets() {
        budgets.values().forEach(counter -> counter.set(DEFAULT_BUDGET));
    }

    /**
     * Consume 1 retry del budget. Devuelve true si aún quedan retries disponibles.
     * Devuelve false si el presupuesto está agotado y ya no debería reintentarse.
     */
    public boolean tryConsumeRetry(String operation) {
        budgets.putIfAbsent(operation, new AtomicInteger(DEFAULT_BUDGET));

        AtomicInteger counter = budgets.get(operation);
        int remaining = counter.decrementAndGet();

        if (remaining < 0) {
            metrics.incrementRetrySkipped(operation);
            return false;
        }

        metrics.incrementRetry(operation);
        return true;
    }

    /**
     * Devuelve cuántos retries quedan disponibles para una operación concreta.
     */
    public int remainingRetries(String operation) {
        return budgets.getOrDefault(operation, new AtomicInteger(DEFAULT_BUDGET)).get();
    }

}
