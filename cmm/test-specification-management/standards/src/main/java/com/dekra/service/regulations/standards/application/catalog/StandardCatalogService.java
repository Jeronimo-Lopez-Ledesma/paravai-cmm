package com.paravai.regulations.standards.application.catalog;

import com.paravai.regulations.standards.domain.value.StandardTypeValue;
import com.paravai.regulations.standards.domain.value.StandardVersionStatusValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class StandardCatalogService {

    public Flux<StandardTypeValue> getStandardTypeCatalog() {
        return Flux.fromIterable(StandardTypeValue.values());
    }

    public Flux<StandardVersionStatusValue> getStandardVersionStatusCatalog() {
        return Flux.fromIterable(StandardVersionStatusValue.values());
    }

}