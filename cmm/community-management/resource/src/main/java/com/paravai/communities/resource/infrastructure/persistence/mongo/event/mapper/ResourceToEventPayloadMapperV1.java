package com.paravai.communities.resource.infrastructure.persistence.mongo.event.mapper;

import com.paravai.communities.contracts.event.resource.ResourceEventPayloadV1;
import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.domain.value.ResourceConditionValue;
import org.springframework.stereotype.Component;

@Component
public class ResourceToEventPayloadMapperV1 {

    public ResourceEventPayloadV1 map(Resource resource) {

        if (resource == null) {
            throw new IllegalArgumentException("Resource must not be null");
        }

        return new ResourceEventPayloadV1(

                resource.id().value(),
                resource.tenantId().value(),
                resource.ownerId().value(),

                resource.title().value(),
                resource.description().orElse(null),

                resource.condition().map(ResourceConditionValue::value).orElse(null),

                resource.createdAt().getInstant(),
                resource.updatedAt().getInstant()
        );
    }
}