package com.paravai.communities.membership.api.rest.v1;

import com.paravai.communities.membership.api.rest.v1.dto.InviteMemberRequest;
import com.paravai.communities.membership.api.rest.v1.dto.MembershipResponse;
import com.paravai.communities.membership.api.rest.v1.dto.RejectMembershipRequest;
import com.paravai.communities.membership.application.command.approve.ApproveMembershipService;
import com.paravai.communities.membership.application.command.invite.InviteMemberService;
import com.paravai.communities.membership.application.command.reject.RejectMembershipService;
import com.paravai.communities.membership.application.command.request.RequestMembershipService;
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
@RequestMapping("/v1/communities/{communityId}")
@Tag(name = "Memberships", description = "Operations related to Community Memberships")
public class MembershipController {

    private static final Logger log = LoggerFactory.getLogger(MembershipController.class);

    private final InviteMemberService inviteService;
    private final RequestMembershipService requestMembershipService;
    private final ApproveMembershipService approveMembershipService;
    private final RejectMembershipService rejectMembershipService;
    private final MessageService messageService;

    public MembershipController(InviteMemberService inviteService,
                                RequestMembershipService requestMembershipService,
                                ApproveMembershipService approveMembershipService,
                                RejectMembershipService rejectMembershipService,
                                MessageService messageService) {
        this.inviteService = Objects.requireNonNull(inviteService, "inviteService");
        this.requestMembershipService = Objects.requireNonNull(requestMembershipService, "requestMembershipService");
        this.approveMembershipService = Objects.requireNonNull(approveMembershipService, "approveMembershipService");
        this.rejectMembershipService = Objects.requireNonNull(rejectMembershipService, "rejectMembershipService");
        this.messageService = Objects.requireNonNull(messageService, "messageService");
    }

    /**
     * EPIC A / A4 - Invite initial members
     *
     * REST surface:
     * - POST /v1/communities/{communityId}/memberships
     *
     * Notes:
     * - tenantId and inviter userOid come from RequestContext
     * - authorization: inviter must be ADMIN
     */
    @PostMapping("/memberships")
    @Operation(summary = "Invite a user to a community (creates a PENDING membership)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invitation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict (already member / duplicate pending invite)", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<MembershipResponse>>> invite(
            @PathVariable("communityId") String communityId,
            @Valid @RequestBody JsonApiRequest<InviteMemberRequest> request,
            @RequestHeader(value = "traceId", required = false) String traceIdHeader,
            @RequestHeader(value = "userOid", required = false) String userOidHeader,
            @RequestHeader(value = "sourceSystem", required = false) String sourceSystemHeader,
            ServerHttpRequest httpRequest
    ) {
        final InviteMemberRequest dto = request.getData().getAttributes();

        return withRequestContext(
                Mono.deferContextual(ctx -> {
                    final String traceId = RequestContext.getTraceId(ctx);
                    final String userOid = RequestContext.getUserOid(ctx);
                    final String tenantId = RequestContext.getTenantId(ctx);
                    final Locale locale = LocaleContext.getOrDefault(ctx);

                    log.debug("[{}][{}] POST /v1/communities/{}/memberships - inviting member (tenantId={})",
                            traceId, userOid, communityId, tenantId);

                    final IdValue tenantIdVo = IdValue.of(tenantId);
                    final IdValue communityIdVo = IdValue.of(communityId);
                    final IdValue inviteeUserIdVo = IdValue.of(dto.getInviteeUserId());
                    final IdValue inviterUserIdVo = IdValue.of(userOid);

                    return inviteService.invite(tenantIdVo, communityIdVo, inviterUserIdVo, inviteeUserIdVo)
                            .map(m -> MembershipResponse.fromDomain(m, locale, messageService))
                            .flatMap(resp ->
                                    JsonApiResponseBuilder.buildSingle(
                                            Mono.just(resp),
                                            httpRequest,
                                            "memberships",
                                            MembershipResponse::getId
                                    ).map(body -> {
                                        URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                                .path("/{id}")
                                                .build(resp.getId());

                                        log.info("[{}][{}] Membership invitation {} created for community {}",
                                                traceId, userOid, resp.getId(), communityId);

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
     * EPIC B / B1 - Request membership
     *
     * REST surface:
     * - POST /v1/communities/{communityId}/membership-requests
     *
     * Notes:
     * - tenantId and requester userOid come from RequestContext
     * - no request body is required for MVP
     */
    @PostMapping("/membership-requests")
    @Operation(summary = "Request access to a community")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membership request created successfully"),
            @ApiResponse(responseCode = "200", description = "Existing pending membership request returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict (already active member or rejected request)", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<MembershipResponse>>> requestMembership(
            @PathVariable("communityId") String communityId,
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
                    final Locale locale = LocaleContext.getOrDefault(ctx);

                    log.debug("[{}][{}] POST /v1/communities/{}/membership-requests - requesting access (tenantId={})",
                            traceId, userOid, communityId, tenantId);

                    final IdValue tenantIdVo = IdValue.of(tenantId);
                    final IdValue communityIdVo = IdValue.of(communityId);
                    final IdValue requesterUserIdVo = IdValue.of(userOid);

                    return requestMembershipService.request(
                                    tenantIdVo,
                                    communityIdVo,
                                    requesterUserIdVo
                            )
                            .flatMap(result -> {
                                MembershipResponse response = MembershipResponse.fromDomain(
                                        result.membership(),
                                        locale,
                                        messageService
                                );

                                return JsonApiResponseBuilder.buildSingle(
                                        Mono.just(response),
                                        httpRequest,
                                        "membership-requests",
                                        MembershipResponse::getId
                                ).map(body -> {
                                    if (result.created()) {
                                        URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                                .path("/{id}")
                                                .build(response.getId());

                                        log.info("[{}][{}] Membership request {} created for community {}",
                                                traceId, userOid, response.getId(), communityId);

                                        return ResponseEntity
                                                .created(location)
                                                .contentType(MediaType.valueOf("application/vnd.api+json"))
                                                .body(body);
                                    }

                                    log.info("[{}][{}] Existing pending membership request {} returned for community {}",
                                            traceId, userOid, response.getId(), communityId);

                                    return ResponseEntity
                                            .ok()
                                            .contentType(MediaType.valueOf("application/vnd.api+json"))
                                            .body(body);
                                });
                            });
                }),
                traceIdHeader,
                userOidHeader,
                sourceSystemHeader
        );
    }

    /**
     * EPIC B / B2 - Approve membership request
     *
     * REST surface:
     * - POST /v1/communities/{communityId}/membership-requests/{membershipId}/approve
     *
     * Notes:
     * - tenantId and approver userOid come from RequestContext
     * - approver must be ADMIN of the community
     * - if membership is already ACTIVE, returns 200 OK with current membership (idempotent)
     */
    @PostMapping("/membership-requests/{membershipId}/approve")
    @Operation(summary = "Approve a membership request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership approved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membership not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<MembershipResponse>>> approveMembership(
            @PathVariable("communityId") String communityId,
            @PathVariable("membershipId") String membershipId,
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
                    final Locale locale = LocaleContext.getOrDefault(ctx);

                    log.debug("[{}][{}] POST /v1/communities/{}/membership-requests/{}/approve (tenantId={})",
                            traceId, userOid, communityId, membershipId, tenantId);

                    final IdValue tenantIdVo = IdValue.of(tenantId);
                    final IdValue communityIdVo = IdValue.of(communityId);
                    final IdValue approverUserIdVo = IdValue.of(userOid);
                    final IdValue membershipIdVo = IdValue.of(membershipId);

                    return approveMembershipService.approve(
                                    tenantIdVo,
                                    communityIdVo,
                                    approverUserIdVo,
                                    membershipIdVo
                            )
                            .flatMap(result -> {
                                MembershipResponse response = MembershipResponse.fromDomain(
                                        result.membership(),
                                        locale,
                                        messageService
                                );

                                return JsonApiResponseBuilder.buildSingle(
                                        Mono.just(response),
                                        "memberships",
                                        MembershipResponse::getId,
                                        resp -> membershipSelfLink(communityId, resp.getId())
                                ).map(body -> {
                                    if (result.changed()) {
                                        log.info("[{}][{}] Membership {} approved in community {}",
                                                traceId, userOid, response.getId(), communityId);
                                    } else {
                                        log.info("[{}][{}] Membership {} already active in community {}",
                                                traceId, userOid, response.getId(), communityId);
                                    }

                                    return ResponseEntity
                                            .ok()
                                            .contentType(MediaType.valueOf("application/vnd.api+json"))
                                            .body(body);
                                });
                            });
                }),
                traceIdHeader,
                userOidHeader,
                sourceSystemHeader
        );
    }

    /**
     * EPIC B / B3 - Reject membership request
     *
     * REST surface:
     * - POST /v1/communities/{communityId}/membership-requests/{membershipId}/reject
     *
     * Notes:
     * - tenantId and rejector userOid come from RequestContext
     * - rejector must be ADMIN of the community
     * - if membership is already REJECTED, returns 200 OK with current membership (idempotent)
     * - rejection reason is optional
     */
    @PostMapping("/membership-requests/{membershipId}/reject")
    @Operation(summary = "Reject a membership request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membership rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Membership not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<MembershipResponse>>> rejectMembership(
            @PathVariable("communityId") String communityId,
            @PathVariable("membershipId") String membershipId,
            @Valid @RequestBody(required = false) JsonApiRequest<RejectMembershipRequest> request,
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
                    final Locale locale = LocaleContext.getOrDefault(ctx);

                    log.debug("[{}][{}] POST /v1/communities/{}/membership-requests/{}/reject (tenantId={})",
                            traceId, userOid, communityId, membershipId, tenantId);

                    final IdValue tenantIdVo = IdValue.of(tenantId);
                    final IdValue communityIdVo = IdValue.of(communityId);
                    final IdValue rejectorUserIdVo = IdValue.of(userOid);
                    final IdValue membershipIdVo = IdValue.of(membershipId);

                    final String reason = extractRejectReason(request);

                    return rejectMembershipService.reject(
                                    tenantIdVo,
                                    communityIdVo,
                                    rejectorUserIdVo,
                                    membershipIdVo,
                                    reason
                            )
                            .flatMap(result -> {
                                MembershipResponse response = MembershipResponse.fromDomain(
                                        result.membership(),
                                        locale,
                                        messageService
                                );

                                return JsonApiResponseBuilder.buildSingle(
                                        Mono.just(response),
                                        "memberships",
                                        MembershipResponse::getId,
                                        resp -> membershipSelfLink(communityId, resp.getId())
                                ).map(body -> {
                                    if (result.changed()) {
                                        log.info("[{}][{}] Membership {} rejected in community {}",
                                                traceId, userOid, response.getId(), communityId);
                                    } else {
                                        log.info("[{}][{}] Membership {} already rejected in community {}",
                                                traceId, userOid, response.getId(), communityId);
                                    }

                                    return ResponseEntity
                                            .ok()
                                            .contentType(MediaType.valueOf("application/vnd.api+json"))
                                            .body(body);
                                });
                            });
                }),
                traceIdHeader,
                userOidHeader,
                sourceSystemHeader
        );
    }

    private static String extractRejectReason(JsonApiRequest<RejectMembershipRequest> request) {
        if (request == null || request.getData() == null || request.getData().getAttributes() == null) {
            return null;
        }
        return request.getData().getAttributes().getReason();
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


    private static String membershipSelfLink(String communityId, String membershipId) {
        return "/v1/communities/" + communityId + "/memberships/" + membershipId;
    }
}