package com.paravai.communities.community.api.rest.v1;


import com.paravai.communities.community.api.rest.v1.dto.ChangeCommunityVisibilityRequest;
import com.paravai.communities.community.api.rest.v1.dto.CommunityResponse;
import com.paravai.communities.community.api.rest.v1.dto.CreateCommunityRequest;
import com.paravai.communities.community.application.command.create.CreateCommunityService;
import com.paravai.communities.community.application.command.visibility.ChangeCommunityVisibilityService;
import com.paravai.communities.community.domain.model.Community;
import com.paravai.communities.community.domain.value.CommunityVisibilityValue;
import com.paravai.foundation.domain.value.IdValue;
import com.paravai.foundation.localization.LocaleContext;
import com.paravai.foundation.localization.MessageService;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiRequest;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiResponseBuilder;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiSingleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

@RestController
@RequestMapping("/v1/communities")
@Tag(name = "Communities", description = "Operations related to Communities")
public class CommunityController {

    private static final Logger log = LoggerFactory.getLogger(CommunityController.class);

    private final CreateCommunityService createService;
    private final ChangeCommunityVisibilityService changeVisibilityService;
    private final MessageService messageService;

    public CommunityController(CreateCommunityService createService,
                               ChangeCommunityVisibilityService changeVisibilityService,
                               MessageService messageService) {
        this.createService = Objects.requireNonNull(createService, "createService");
        this.changeVisibilityService = Objects.requireNonNull(changeVisibilityService, "changeVisibiltyService");
        this.messageService = Objects.requireNonNull(messageService, "messageService");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Community")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Community created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Domain conflict (duplicate business identity)", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<CommunityResponse>>> create(
            @Valid @RequestBody JsonApiRequest<CreateCommunityRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final CreateCommunityRequest dto = request.getData().getAttributes();

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String tenantId = RequestContext.getTenantId(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] POST /v1/communities - creating community (tenantId={})",
                    traceId, userOid, tenantId);

            // Multi-tenant boundary and creator identity always come from context, not from payload
            final IdValue tenantIdVo = IdValue.of(tenantId);
            final IdValue createdByVo = IdValue.of(userOid);

            final Community toCreate = dto.toDomain(tenantIdVo, createdByVo);

            return createService.create(toCreate)
                    .map(c -> CommunityResponse.fromDomain(c, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "communities",
                                    CommunityResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .path("/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Community {} created", traceId, userOid, resp.getId());

                                return ResponseEntity
                                        .created(location)
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });
    }


    @PatchMapping("/{id}/visibility")
    @Operation(summary = "Change community visibility (PUBLIC/PRIVATE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visibility updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<CommunityResponse>>> changeVisibility(
            @PathVariable("id") String id,
            @Valid @RequestBody JsonApiRequest<ChangeCommunityVisibilityRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final ChangeCommunityVisibilityRequest dto = request.getData().getAttributes();

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] PATCH /v1/communities/{}/visibility", traceId, userOid, id);

            final IdValue communityId = IdValue.of(id);
            final var visibility = CommunityVisibilityValue.of(dto.getVisibilityCode());

            return changeVisibilityService.changeVisibility(communityId, visibility)
                    .map(c -> CommunityResponse.fromDomain(c, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "communities",
                                    CommunityResponse::getId
                            ).map(body -> ResponseEntity
                                    .ok()
                                    .contentType(MediaType.valueOf("application/vnd.api+json"))
                                    .body(body))
                    );
        });
    }
}