package com.paravai.foundation.infrastructure.kafka.inbound.dlq;

import reactor.core.publisher.Mono;

public interface InboundDlqPublisher {

    Mono<Void> publishToDlq(
            String dlqTopic,
            String key,
            String payload,
            Throwable error
    );
}