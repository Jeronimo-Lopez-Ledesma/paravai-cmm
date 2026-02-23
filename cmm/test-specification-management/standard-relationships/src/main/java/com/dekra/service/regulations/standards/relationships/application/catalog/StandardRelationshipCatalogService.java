package com.paravai.regulations.standards.relationships.application.catalog;

import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipPurposeValue;
import com.paravai.regulations.standards.relationships.domain.value.StandardRelationshipTypeValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class StandardRelationshipCatalogService {

    public Flux<StandardRelationshipTypeValue> getRelationshipTypeCatalog() {
        return Flux.fromIterable(StandardRelationshipTypeValue.values());
    }

    public Flux<StandardRelationshipPurposeValue> getRelationshipPurposeCatalog() {
        return Flux.fromIterable(StandardRelationshipPurposeValue.values());
    }
}
