package com.paravai.regulations.standards.relationships.infrastructure.persistence.mongo.springdata;
import com.paravai.regulations.standards.relationships.infrastructure.persistence.mongo.document.StandardRelationshipDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface StandardRelationshipSpringReactiveMongoRepository extends ReactiveMongoRepository<StandardRelationshipDocument, String> {

}