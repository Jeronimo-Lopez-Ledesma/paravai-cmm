package com.paravai.communities.resource.api.rest.v1;

import com.paravai.communities.resource.api.rest.v1.dto.RegisterResourceRequest;
import com.paravai.communities.resource.api.rest.v1.dto.ResourceResponse;
import com.paravai.communities.resource.application.command.register.RegisterResourceService;
import com.paravai.communities.resource.application.query.get.GetResourceService;
import com.paravai.foundation.domain.value.IdValue;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/v1/resources")
@Tag(name = "Resources", description = "Operations related to user-owned resources")
public class ResourceController {

    private static final Logger log = LoggerFactory.getLogger(ResourceController.class);

    private final RegisterResourceService registerResourceService;
    private final GetResourceService getResourceService;

    public ResourceController(RegisterResourceService registerResourceService,
                              GetResourceService getResourceService) {
        this.registerResourceService = Objects.requireNonNull(registerResourceService, "registerResourceService");
        this.getResourceService = Objects.requireNonNull(getResourceService, "getResourceService");
    }



    /**
     * EPIC C / C1 - Register resource
     *
     * REST surface:
     * - POST /v1/resources
     *
     * Notes:
     * - tenantId and owner userOid come from RequestContext
     * - resource is always created for the authenticated user
     */
    @PostMapping
    @Operation(summary = "Register a new resource owned by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Resource created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<ResourceResponse>>> register(
            @Valid @RequestBody JsonApiRequest<RegisterResourceRequest> request,
            @RequestHeader(value = "traceId", required = false) String traceIdHeader,
            @RequestHeader(value = "userOid", required = false) String userOidHeader,
            @RequestHeader(value = "sourceSystem", required = false) String sourceSystemHeader,
            ServerHttpRequest httpRequest
    ) {
        final RegisterResourceRequest dto = request.getData().getAttributes();

        return withRequestContext(
                Mono.deferContextual(ctx -> {
                    final String traceId = RequestContext.getTraceId(ctx);
                    final String userOid = RequestContext.getUserOid(ctx);
                    final String tenantId = RequestContext.getTenantId(ctx);

                    if ("anonymous".equals(userOid)) {
                        return Mono.error(new IllegalArgumentException("User must be authenticated"));
                    }

                    log.debug("[{}][{}] POST /v1/resources - registering resource (tenantId={})",
                            traceId, userOid, tenantId);

                    final IdValue tenantIdVo = IdValue.of(tenantId);
                    final IdValue ownerIdVo = IdValue.of(userOid);

                    return registerResourceService.register(
                                    tenantIdVo,
                                    ownerIdVo,
                                    dto.getTitle(),
                                    dto.getDescription(),
                                    dto.getConditionCode()
                            )
                            .map(result -> ResourceResponse.fromDomain(result.resource()))
                            .flatMap(resp ->
                                    JsonApiResponseBuilder.buildSingle(
                                            Mono.just(resp),
                                            httpRequest,
                                            "resources",
                                            ResourceResponse::getId
                                    ).map(body -> {
                                        URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                                .path("/{id}")
                                                .build(resp.getId());

                                        log.info("[{}][{}] Resource {} created",
                                                traceId, userOid, resp.getId());

                                        return ResponseEntity
                                                .created(location)
                                                .contentType(MediaType.valueOf("application/vnd.api+json"))
                                                .body(body);
                                    })
                            );
                }),
                traceIdHeader,
                userOidHeader,
                sourceSystemHeader
        );
    }

    /**
     * EPIC C / C2 - Get resource by id
     *
     * REST surface:
     * - GET /v1/resources/{resourceId}
     *
     * Notes:
     * - tenantId and owner userOid come from RequestContext
     * - only the owner can access the resource
     * - if the resource does not exist OR does not belong to the user, returns 404
     * - no events are emitted
     */
    @GetMapping("/{resourceId}")
    @Operation(summary = "Get a resource by id (owner-only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resource retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Resource not found", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<ResourceResponse>>> getById(
            @PathVariable("resourceId") String resourceId,
            @RequestHeader(value = "traceId", required = false) String traceIdHeader,
            @RequestHeader(value = "userOid", required = false) String userOidHeader,
            @RequestHeader(value = "sourceSystem", required = false) String sourceSystemHeader
    ) {
        return withRequestContext(
                Mono.deferContextual(ctx -> {
                    final String traceId = RequestContext.getTraceId(ctx);
                    final String userOid = RequestContext.getUserOid(ctx);
                    final String tenantId = RequestContext.getTenantId(ctx);

                    if ("anonymous".equals(userOid)) {
                        return Mono.error(new IllegalArgumentException("User must be authenticated"));
                    }

                    log.debug("[{}][{}] GET /v1/resources/{} (tenantId={})",
                            traceId, userOid, resourceId, tenantId);

                    final IdValue resourceIdVo = IdValue.of(resourceId);
                    final IdValue ownerIdVo = IdValue.of(userOid);

                    return getResourceService.get(resourceIdVo, ownerIdVo)
                            .map(result -> ResourceResponse.fromDomain(result.resource()))
                            .flatMap(resp ->
                                    JsonApiResponseBuilder.buildSingle(
                                            Mono.just(resp),
                                            "resources",
                                            ResourceResponse::getId,
                                            r -> resourceSelfLink(r.getId())
                                    ).map(body -> {
                                        log.info("[{}][{}] Resource {} retrieved",
                                                traceId, userOid, resourceId);

                                        return ResponseEntity
                                                .ok()
                                                .contentType(MediaType.valueOf("application/vnd.api+json"))
                                                .body(body);
                                    })
                            );
                }),
                traceIdHeader,
                userOidHeader,
                sourceSystemHeader
        );
    }

    private static String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private <T> Mono<T> withRequestContext(
            Mono<T> mono,
            String traceIdHeader,
            String userOidHeader,
            String sourceSystemHeader
    ) {
        final String traceId = defaultIfBlank(traceIdHeader, "missing-trace-id");
        final String userOid = defaultIfBlank(userOidHeader, "anonymous");
        final String sourceSystem = defaultIfBlank(sourceSystemHeader, "unknown");

        return mono.contextWrite(ctx -> ctx
                .put(RequestContext.TRACE_ID_KEY, traceId)
                .put(RequestContext.USER_OID_KEY, userOid)
                .put(RequestContext.SOURCE_SYSTEM_KEY, sourceSystem)
        );

    }

    private static String resourceSelfLink(String resourceId) {
        return "/v1/resources/" + resourceId;
    }
}