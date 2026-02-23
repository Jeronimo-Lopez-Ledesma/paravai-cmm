package com.paravai.foundation.domain.event;

public abstract class AbstractReactiveDomainEventHandler<T extends DomainEvent> implements ReactiveDomainEventHandler<T> {

    private final Class<T> eventType;

    protected AbstractReactiveDomainEventHandler(Class<T> eventType) {
        this.eventType = eventType;
    }

    @Override
    public Class<T> eventType() {
        return eventType;
    }
}
