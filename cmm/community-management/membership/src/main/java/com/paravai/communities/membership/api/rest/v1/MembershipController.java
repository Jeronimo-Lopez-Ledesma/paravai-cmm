package com.paravai.communities.membership.api.rest.v1;

import com.paravai.communities.membership.api.rest.v1.dto.InviteMemberRequest;
import com.paravai.communities.membership.api.rest.v1.dto.MembershipResponse;
import com.paravai.communities.membership.application.command.invite.InviteMemberService;
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
@RequestMapping("/v1/communities/{communityId}/memberships")
@Tag(name = "Memberships", description = "Operations related to Community Memberships")
public class MembershipController {

    private static final Logger log = LoggerFactory.getLogger(MembershipController.class);

    private final InviteMemberService inviteService;
    private final MessageService messageService;

    public MembershipController(InviteMemberService inviteService,
                                MessageService messageService) {
        this.inviteService = Objects.requireNonNull(inviteService, "inviteService");
        this.messageService = Objects.requireNonNull(messageService, "messageService");
    }

    /**
     * EPIC A / A4 - Invite initial members
     *
     * REST surface:
     * - POST /v1/communities/{communityId}/memberships (represents invitation as Membership with status=PENDING)
     *
     * Notes:
     * - tenantId and inviter userOid come from RequestContext (not from payload)
     * - authorization: inviter must be ADMIN (resolved through Membership aggregate/repo)
     */
    @PostMapping
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
            ServerHttpRequest httpRequest
    ) {
        final InviteMemberRequest dto = request.getData().getAttributes();

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final String tenantId = RequestContext.getTenantId(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] POST /v1/communities/{}/memberships - inviting member (tenantId={})",
                    traceId, userOid, communityId, tenantId);

            final IdValue tenantIdVo = IdValue.of(tenantId);
            final IdValue communityIdVo = IdValue.of(communityId);

            // For MVP: invitee comes from payload
            final IdValue inviteeUserIdVo = IdValue.of(dto.getInviteeUserId());

            // Inviter comes from context (for permission check)
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
        });
    }
}