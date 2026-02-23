package com.paravai.regulations.standards.infrastructure.persistence.mongo.springdata;

import com.paravai.regulations.standards.infrastructure.persistence.mongo.document.StandardDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface StandardSpringReactiveMongoRepository extends ReactiveMongoRepository<StandardDocument, String> {

}