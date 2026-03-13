package com.paravai.foundation.infrastructure.kafka.inbound.dlq;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import reactor.kafka.receiver.ReceiverRecord;

public interface InboundDlqTopicResolver {

    String resolve(ReceiverRecord<String, byte[]> record);
}