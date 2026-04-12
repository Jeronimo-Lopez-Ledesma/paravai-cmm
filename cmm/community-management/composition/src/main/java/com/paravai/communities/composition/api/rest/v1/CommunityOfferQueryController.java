package com.paravai.communities.composition.api.rest.v1;

import com.paravai.communities.composition.api.rest.v1.dto.OfferResponse;
import com.paravai.communities.composition.offer.application.listcommunity.ListCommunityOffersOrchestrator;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiResponse;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiResponseBuilder;
import com.paravai.foundation.viewjsonapi.pagination.PaginationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@RequestMapping("/v1/communities")
@Tag(name = "Community Offers", description = "Operations to browse offers inside communities")
public class CommunityOfferQueryController {

    private static final Logger log = LoggerFactory.getLogger(CommunityOfferQueryController.class);

    private final ListCommunityOffersOrchestrator listCommunityOffersOrchestrator;

    public CommunityOfferQueryController(
            ListCommunityOffersOrchestrator listCommunityOffersOrchestrator
    ) {
        this.listCommunityOffersOrchestrator = Objects.requireNonNull(
                listCommunityOffersOrchestrator,
                "listCommunityOffersOrchestrator"
        );
    }
    /**
     * EPIC D / D1 - Explore active offers of a community
     *
     * REST surface:
     * - GET /v1/communities/{communityId}/offers?page=...&size=...
     *
     * Notes:
     * - tenantId and userOid come from RequestContext
     * - only ACTIVE members of the community can access this listing
     * - only ACTIVE offers are returned
     * - supports basic pagination
     * - no events are emitted
     */

    @GetMapping("/{communityId}/offers")
    @Operation(summary = "List ACTIVE offers visible inside a community")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Community offers retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid pagination", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiResponse<OfferResponse>>> listCommunityOffers(
            @PathVariable("communityId") String communityId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
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

                    log.debug("[{}][{}] GET /v1/communities/{}/offers?page={}&size={} (tenantId={})",
                            traceId, userOid, communityId, effectivePage, effectiveSize, tenantId);

                    Flux<OfferResponse> dataFlux = listCommunityOffersOrchestrator.list(
                                    tenantId,
                                    communityId,
                                    effectivePage,
                                    effectiveSize
                            )
                            .map(OfferResponse::fromSummary);

                    Mono<Long> totalMono = listCommunityOffersOrchestrator.count(
                            tenantId,
                            communityId
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
                                log.info("[{}][{}] Retrieved paginated ACTIVE offers for community {}",
                                        traceId, userOid, communityId);

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