package com.paravai.foundation.persistence.mongo;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class MongoReactiveEntityFilter<T, D> {

    private final ReactiveMongoTemplate mongoTemplate;
    private final Class<D> documentClass;
    private final Function<D, T> toDomainMapper;
    private final String defaultSearchField;

    public MongoReactiveEntityFilter(ReactiveMongoTemplate mongoTemplate,
                                     Class<D> documentClass,
                                     Function<D, T> toDomainMapper,
                                     String defaultSearchField) {
        this.mongoTemplate = mongoTemplate;
        this.documentClass = documentClass;
        this.toDomainMapper = toDomainMapper;
        this.defaultSearchField = defaultSearchField;
    }

    public Flux<T> findByFilters(Map<String, String> filters,
                                 Optional<String> search,
                                 Optional<String> sort,
                                 int page,
                                 int size) {

        Query query = MongoQueryBuilder.buildQuery(filters, search, sort, page, size, defaultSearchField);
        return mongoTemplate.find(query, documentClass)
                .map(toDomainMapper);
    }

    public Mono<Long> countByFilters(Map<String, String> filters,
                                     Optional<String> search) {

        Query query = MongoQueryBuilder.buildQuery(filters, search);
        return mongoTemplate.count(query, documentClass);
    }
}
