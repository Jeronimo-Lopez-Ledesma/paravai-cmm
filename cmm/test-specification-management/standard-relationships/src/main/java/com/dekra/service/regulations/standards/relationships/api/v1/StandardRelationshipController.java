package com.dekra.service.regulations.standards.relationships.api.v1;

import com.dekra.service.foundation.domaincore.value.IdValue;
import com.dekra.service.foundation.localization.LocaleContext;
import com.dekra.service.foundation.localization.MessageService;
import com.dekra.service.foundation.securityutils.reactive.context.RequestContext;
import com.dekra.service.foundation.viewjsonapi.jsonapi.JsonApiRequest;
import com.dekra.service.foundation.viewjsonapi.jsonapi.JsonApiResponseBuilder;
import com.dekra.service.foundation.viewjsonapi.jsonapi.JsonApiSingleResponse;
import com.dekra.service.regulations.standards.relationships.api.v1.dto.CreateStandardRelationshipRequest;
import com.dekra.service.regulations.standards.relationships.api.v1.dto.StandardRelationshipResponse;
import com.dekra.service.regulations.standards.relationships.application.command.create.CreateStandardRelationshipService;
import com.dekra.service.regulations.standards.relationships.application.command.delete.DeleteStandardRelationshipService;
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

@RestController
@RequestMapping("/v1/standards/relationships")
@Tag(name = "Standard Relationships", description = "Operations related to relationships between Standard Versions")
public class StandardRelationshipController {

    private static final Logger log = LoggerFactory.getLogger(StandardRelationshipController.class);

    private final CreateStandardRelationshipService createService;
    private final DeleteStandardRelationshipService deleteService;
    private final MessageService messageService;

    public StandardRelationshipController(CreateStandardRelationshipService createService,
                                          DeleteStandardRelationshipService deleteService,
                                          MessageService messageService) {
        this.createService = createService;
        this.deleteService = deleteService;
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a relationship between two Standard Versions")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Relationship created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Domain conflict / duplicate relationship", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardRelationshipResponse>>> create(
            @Valid @RequestBody JsonApiRequest<CreateStandardRelationshipRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final CreateStandardRelationshipRequest dto = request.getData().getAttributes();

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] POST /v1/standards/relationships - creating relationship", traceId, userOid);

            return createService.create(
                            dto.toFromRefVo(),
                            dto.toToRefVo(),
                            dto.toTypeVo(),
                            dto.toPurposeVoOrNull()
                    )
                    .map(rel -> StandardRelationshipResponse.fromDomain(rel, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standard-relationships",
                                    StandardRelationshipResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .path("/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Relationship {} created", traceId, userOid, resp.getId());

                                return ResponseEntity
                                        .created(location)
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a Standard Relationship by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Relationship deleted (or did not exist)"),
            @ApiResponse(responseCode = "400", description = "Invalid id", content = @Content)
    })
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable("id") String id) {
        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);

            log.debug("[{}][{}] DELETE /v1/standards/relationships/{} - deleting relationship", traceId, userOid, id);

            return deleteService.deleteById(IdValue.of(id))
                    .doOnSuccess(__ -> log.info("[{}][{}] Relationship {} deleted", traceId, userOid, id))
                    .thenReturn(ResponseEntity.noContent().build());
        });
    }
}
