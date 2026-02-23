package com.paravai.regulations.standards.domain.repository;

import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import com.paravai.regulations.standards.domain.model.Standard;
import com.paravai.regulations.standards.domain.value.StandardCodeValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Domain port (Hexagonal Architecture).
 * No Spring, no Mongo, no DTOs.
 */
public interface StandardRepository {


    Mono<Standard> save(Standard standard);

    Mono<Standard> findById(IdValue id);

    Mono<Void> deleteById(IdValue id);

    // Search with pagination (filters + search + sort + pagination)
    Flux<Standard> search(SearchQueryValue query);

    // Needed for pagination
    Mono<Long> count(SearchQueryValue query);

}
