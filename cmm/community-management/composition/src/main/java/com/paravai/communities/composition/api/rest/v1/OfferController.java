package com.paravai.communities.composition.api.rest.v1;

import com.paravai.communities.composition.api.rest.v1.dto.OfferResponse;
import com.paravai.communities.composition.api.rest.v1.dto.PublishOfferRequest;
import com.paravai.communities.composition.api.rest.v1.dto.UpdateOfferAvailabilityRequest;
import com.paravai.communities.composition.offer.application.pause.PauseOfferOrchestrator;
import com.paravai.communities.composition.offer.application.publish.PublishOfferOrchestrator;
import com.paravai.communities.composition.offer.application.updateavailability.UpdateOfferAvailabilityOrchestrator;
import com.paravai.communities.composition.offer.application.withdraw.WithdrawOfferOrchestrator;
import com.paravai.communities.composition.offer.application.listmy.ListMyOffersOrchestrator;
import com.paravai.communities.composition.offer.application.listcommunity.ListCommunityOffersOrchestrator;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiResponse;
import com.paravai.foundation.viewjsonapi.pagination.PaginationRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/v1/offers")
@Tag(name = "Offers", description = "Operations related to community offers")
public class OfferController {

    private static final Logger log = LoggerFactory.getLogger(OfferController.class);

    private final PublishOfferOrchestrator publishOfferOrchestrator;
    private final UpdateOfferAvailabilityOrchestrator updateOfferAvailabilityOrchestrator;
    private final PauseOfferOrchestrator pauseOfferOrchestrator;
    private final WithdrawOfferOrchestrator withdrawOfferOrchestrator;
    private final ListMyOffersOrchestrator listMyOffersOrchestrator;
    private final ListCommunityOffersOrchestrator listCommunityOffersOrchestrator;


    public OfferController(PublishOfferOrchestrator publishOfferOrchestrator,
                           UpdateOfferAvailabilityOrchestrator updateOfferAvailabilityOrchestrator,
                           PauseOfferOrchestrator pauseOfferOrchestrator,
                           WithdrawOfferOrchestrator withdrawOfferOrchestrator,
                           ListMyOffersOrchestrator listMyOffersOrchestrator,
                           ListCommunityOffersOrchestrator listCommunityOffersOrchestrator) {

        this.publishOfferOrchestrator = Objects.requireNonNull(publishOfferOrchestrator, "publishOfferOrchestrator");
        this.updateOfferAvailabilityOrchestrator = Objects.requireNonNull(updateOfferAvailabilityOrchestrator, "updateOfferAvailabilityOrchestrator");
        this.pauseOfferOrchestrator = Objects.requireNonNull(pauseOfferOrchestrator, "pauseOfferOrchestrator");
        this.withdrawOfferOrchestrator = Objects.requireNonNull(withdrawOfferOrchestrator, "withdrawOfferOrchestrator");
        this.listMyOffersOrchestrator = Objects.requireNonNull(listMyOffersOrchestrator, "listMyOffersOrchestrator");
        this.listCommunityOffersOrchestrator = Objects.requireNonNull(listCommunityOffersOrchestrator, "listCommunityOffersOrchestrator");
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

    /**
     * EPIC C / C3 - Update offer availability
     *
     * REST surface:
     * - PUT /v1/offers/{offerId}/availability
     *
     * Notes:
     * - acting userOid comes from RequestContext
     * - only the owner can update availability
     * - WITHDRAWN offers cannot update availability
     * - locked offers cannot update availability
     * - idempotent if the requested availability is already the current one
     */
    @PutMapping("/{offerId}/availability")
    @Operation(summary = "Update availability of an existing offer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer availability updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<OfferResponse>>> updateAvailability(
            @PathVariable("offerId") String offerId,
            @Valid @RequestBody JsonApiRequest<UpdateOfferAvailabilityRequest> request,
            @RequestHeader(value = "traceId", required = false) String traceIdHeader,
            @RequestHeader(value = "userOid", required = false) String userOidHeader,
            @RequestHeader(value = "sourceSystem", required = false) String sourceSystemHeader,
            ServerHttpRequest httpRequest
    ) {
        return withRequestContext(
                Mono.deferContextual(ctx -> {
                    final String traceId = RequestContext.getTraceId(ctx);
                    final String userOid = RequestContext.getUserOid(ctx);

                    if ("anonymous".equals(userOid)) {
                        return Mono.error(new IllegalArgumentException("User must be authenticated"));
                    }

                    final UpdateOfferAvailabilityRequest dto = extractAttributes(request);

                    log.debug("[{}][{}] PUT /v1/offers/{}/availability - availabilityStatusCode={}",
                            traceId, userOid, offerId, dto.getAvailabilityStatusCode());

                    return updateOfferAvailabilityOrchestrator.update(
                                    IdValue.of(offerId),
                                    dto.getAvailabilityStatusCode()
                            )
                            .map(result -> OfferResponse.fromSummary(result.offer()))
                            .flatMap(resp ->
                                    JsonApiResponseBuilder.buildSingle(
                                            Mono.just(resp),
                                            httpRequest,
                                            "offers",
                                            OfferResponse::getId
                                    ).map(body -> {
                                        log.info("[{}][{}] Offer {} availability updated to {}",
                                                traceId, userOid, offerId, dto.getAvailabilityStatusCode());

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

    /**
     * EPIC C / C4 - Pause offer
     *
     * REST surface:
     * - POST /v1/offers/{offerId}/pause
     *
     * Notes:
     * - acting userOid comes from RequestContext
     * - only the owner can pause the offer
     * - only ACTIVE offers can transition to PAUSED
     * - WITHDRAWN offers cannot be paused
     * - idempotent if the offer is already PAUSED
     */
    @PostMapping("/{offerId}/pause")
    @Operation(summary = "Pause an existing offer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer paused successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<OfferResponse>>> pause(
            @PathVariable("offerId") String offerId,
            @RequestHeader(value = "traceId", required = false) String traceIdHeader,
            @RequestHeader(value = "userOid", required = false) String userOidHeader,
            @RequestHeader(value = "sourceSystem", required = false) String sourceSystemHeader,
            ServerHttpRequest httpRequest
    ) {
        return withRequestContext(
                Mono.deferContextual(ctx -> {
                    final String traceId = RequestContext.getTraceId(ctx);
                    final String userOid = RequestContext.getUserOid(ctx);

                    if ("anonymous".equals(userOid)) {
                        return Mono.error(new IllegalArgumentException("User must be authenticated"));
                    }

                    log.debug("[{}][{}] POST /v1/offers/{}/pause",
                            traceId, userOid, offerId);

                    return pauseOfferOrchestrator.pause(IdValue.of(offerId))
                            .map(result -> OfferResponse.fromSummary(result.offer()))
                            .flatMap(resp ->
                                    JsonApiResponseBuilder.buildSingle(
                                            Mono.just(resp),
                                            "offers",
                                            OfferResponse::getId,
                                            item -> "/v1/offers/" + item.getId()
                                    ).map(body -> {
                                        log.info("[{}][{}] Offer {} paused",
                                                traceId, userOid, offerId);

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

    /**
     * EPIC C / C5 - Withdraw offer
     *
     * REST surface:
     * - DELETE /v1/offers/{offerId}
     *
     * Notes:
     * - acting userOid comes from RequestContext
     * - only the owner can withdraw the offer
     * - only ACTIVE or PAUSED offers can transition to WITHDRAWN
     * - locked offers cannot be withdrawn
     * - idempotent if the offer is already WITHDRAWN
     */
    @DeleteMapping("/{offerId}")
    @Operation(summary = "Withdraw an existing offer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offer withdrawn successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Offer not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<OfferResponse>>> withdraw(
            @PathVariable("offerId") String offerId,
            @RequestHeader(value = "traceId", required = false) String traceIdHeader,
            @RequestHeader(value = "userOid", required = false) String userOidHeader,
            @RequestHeader(value = "sourceSystem", required = false) String sourceSystemHeader,
            ServerHttpRequest httpRequest
    ) {
        return withRequestContext(
                Mono.deferContextual(ctx -> {
                    final String traceId = RequestContext.getTraceId(ctx);
                    final String userOid = RequestContext.getUserOid(ctx);

                    if ("anonymous".equals(userOid)) {
                        return Mono.error(new IllegalArgumentException("User must be authenticated"));
                    }

                    log.debug("[{}][{}] DELETE /v1/offers/{}",
                            traceId, userOid, offerId);

                    return withdrawOfferOrchestrator.withdraw(IdValue.of(offerId))
                            .map(OfferResponse::fromSummary)
                            .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    "offers",
                                    OfferResponse::getId,
                                    item -> "/v1/offers/" + item.getId()
                            ).map(body -> {
                                log.info("[{}][{}] Offer {} withdrawn",
                                        traceId, userOid, offerId);

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

    /**
     * EPIC C / C6 - List my offers
     *
     * REST surface:
     * - GET /v1/offers/me?page=...&size=...&status=...
     *
     * Notes:
     * - tenantId and owner userOid come from RequestContext
     * - only offers owned by the authenticated user are returned
     * - supports basic pagination
     * - optional filtering by status
     * - no events are emitted
     */
    @GetMapping("/me")
    @Operation(summary = "List offers owned by the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Offers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or status filter", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiResponse<OfferResponse>>> listMyOffers(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "status", required = false) String status,
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

                    final int effectivePage = (page == null ? 1 : page);
                    final int effectiveSize = (size == null ? 20 : size);

                    log.debug("[{}][{}] GET /v1/offers/me?page={}&size={}&status={} (tenantId={})",
                            traceId, userOid, effectivePage, effectiveSize, status, tenantId);

                    Flux<OfferResponse> dataFlux = listMyOffersOrchestrator.list(
                                    tenantId,
                                    userOid,
                                    status,
                                    effectivePage,
                                    effectiveSize
                            )
                            .map(OfferResponse::fromSummary);

                    Mono<Long> totalMono = listMyOffersOrchestrator.count(
                            tenantId,
                            userOid,
                            status
                    );

                    PaginationRequest pagination = new PaginationRequest(effectivePage, effectiveSize);

                    return JsonApiResponseBuilder.buildPaginated(
                                    dataFlux,
                                    totalMono,
                                    pagination,
                                    httpRequest,
                                    "offers",
                                    OfferResponse::getId
                            )
                            .map(body -> {
                                log.info("[{}][{}] Retrieved paginated offers for owner {}",
                                        traceId, userOid, userOid);

                                return ResponseEntity
                                        .ok()
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            });
                }),
                traceIdHeader,
                userOidHeader,
                sourceSystemHeader
        );
    }

    private static <T> T extractAttributes(JsonApiRequest<T> request) {
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