package com.paravai.foundation.persistence.mongo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MongoQueryExecutor<T> {

    private final ReactiveMongoTemplate mongoTemplate;
    private final Class<T> documentClass;

    public MongoQueryExecutor(ReactiveMongoTemplate mongoTemplate, Class<T> documentClass) {
        this.mongoTemplate = mongoTemplate;
        this.documentClass = documentClass;
    }

    public Flux<T> find(Query query, Pageable pageable) {
        return mongoTemplate.find(query.with(pageable), documentClass);
    }

    public Mono<Long> count(Query query) {
        return mongoTemplate.count(query, documentClass);
    }
}
