package com.paravai.communities.composition.api.rest.v1;

import com.paravai.communities.composition.api.rest.v1.dto.OfferResponse;
import com.paravai.communities.composition.api.rest.v1.dto.PublishOfferRequest;
import com.paravai.communities.composition.offer.application.publish.PublishOfferOrchestrator;
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
@RequestMapping("/v1/offers")
@Tag(name = "Offers", description = "Operations related to community offers")
public class OfferController {

    private static final Logger log = LoggerFactory.getLogger(OfferController.class);

    private final PublishOfferOrchestrator publishOfferOrchestrator;

    public OfferController(PublishOfferOrchestrator publishOfferOrchestrator) {
        this.publishOfferOrchestrator = Objects.requireNonNull(publishOfferOrchestrator, "publishOfferOrchestrator");
    }

    /**
     * EPIC C / C2 - Publish an offer in a community
     *
     * REST surface:
     * - POST /v1/offers
     *
     * Notes:
     * - tenantId and acting userOid come from RequestContext
     * - the authenticated user must be ACTIVE in the target community
     * - the referenced resource must exist and belong to the authenticated user
     * - the exchange type must be allowed by the community rules
     * - only one ACTIVE offer is allowed per (tenantId, communityId, resourceId)
     */
    @PostMapping
    @Operation(summary = "Publish a new offer for a resource in a community")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Offer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Community or resource not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<OfferResponse>>> publish(
            @Valid @RequestBody JsonApiRequest<PublishOfferRequest> request,
            @RequestHeader(value = "traceId", required = false) String traceIdHeader,
            @RequestHeader(value = "userOid", required = false) String userOidHeader,
            @RequestHeader(value = "sourceSystem", required = false) String sourceSystemHeader,
            ServerHttpRequest httpRequest
    ) {
        return withRequestContext(
                Mono.deferContextual(ctx -> {
                    final String traceId = RequestContext.getTraceId(ctx);
                    final String userOid = RequestContext.getUserOid(ctx);
                    final String tenantId = RequestContext.getTenantId(ctx);

                    if ("anonymous".equals(userOid)) {
                        return Mono.error(new IllegalArgumentException("User must be authenticated"));
                    }

                    final PublishOfferRequest dto = extractAttributes(request);

                    log.debug("[{}][{}] POST /v1/offers - publishing offer (tenantId={}, communityId={}, resourceId={})",
                            traceId, userOid, tenantId, dto.getCommunityId(), dto.getResourceId());

                    final IdValue tenantIdVo = IdValue.of(tenantId);
                    final IdValue communityIdVo = IdValue.of(dto.getCommunityId());
                    final IdValue resourceIdVo = IdValue.of(dto.getResourceId());
                    final IdValue actingUserIdVo = IdValue.of(userOid);

                    return publishOfferOrchestrator.publish(
                                    tenantIdVo,
                                    communityIdVo,
                                    resourceIdVo,
                                    actingUserIdVo,
                                    dto.getExchangeTypeCode(),
                                    dto.getDescription()
                            )
                            .map(result -> OfferResponse.fromSummary(result.offer()))
                            .flatMap(resp ->
                                    JsonApiResponseBuilder.buildSingle(
                                            Mono.just(resp),
                                            httpRequest,
                                            "offers",
                                            OfferResponse::getId
                                    ).map(body -> {
                                        URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                                .path("/{id}")
                                                .build(resp.getId());

                                        log.info("[{}][{}] Offer {} created for resource {} in community {}",
                                                traceId, userOid, resp.getId(), dto.getResourceId(), dto.getCommunityId());

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

    private static PublishOfferRequest extractAttributes(JsonApiRequest<PublishOfferRequest> request) {
        if (request == null || request.getData() == null || request.getData().getAttributes() == null) {
            throw new IllegalArgumentException("Request body must contain data.attributes");
        }
        return request.getData().getAttributes();
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
}