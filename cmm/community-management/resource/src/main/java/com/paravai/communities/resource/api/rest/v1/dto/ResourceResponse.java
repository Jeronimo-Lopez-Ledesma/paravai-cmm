package com.paravai.communities.resource.api.rest.v1.dto;

import com.paravai.communities.resource.domain.model.Resource;
import com.paravai.communities.resource.domain.value.ResourceConditionValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {

    private String id;

    private String tenantId;
    private String ownerId;

    private String title;
    private String description;

    private String conditionCode;

    private Instant createdAt;
    private Instant updatedAt;

    public static ResourceResponse fromDomain(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.id().value())
                .tenantId(resource.tenantId().value())
                .ownerId(resource.ownerId().value())
                .title(resource.title().value())
                .description(resource.description().orElse(null))
                .conditionCode(resource.condition().map(ResourceConditionValue::value).orElse(null))
                .createdAt(resource.createdAt().getInstant())
                .updatedAt(resource.updatedAt().getInstant())
                .build();
    }
}