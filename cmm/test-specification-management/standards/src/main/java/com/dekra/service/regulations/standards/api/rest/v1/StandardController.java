package com.paravai.regulations.standards.api.rest.v1;

import com.paravai.foundation.domain.organization.value.OrganizationAssociationValue;
import com.paravai.foundation.domaincore.value.IdValue;
import com.paravai.foundation.localization.LocaleContext;
import com.paravai.foundation.localization.MessageService;
import com.paravai.foundation.securityutils.reactive.context.RequestContext;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiRequest;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiResponseBuilder;
import com.paravai.foundation.viewjsonapi.jsonapi.JsonApiSingleResponse;
import com.paravai.foundation.viewjsonapi.pagination.PaginationRequest;
import com.paravai.foundation.viewjsonapi.query.SearchQueryValue;
import com.paravai.regulations.standards.api.rest.v1.dto.*;
import com.paravai.regulations.standards.application.command.applicability.AddApplicabilityContextService;
import com.paravai.regulations.standards.application.command.applicability.RemoveApplicabilityContextService;
import com.paravai.regulations.standards.application.command.create.CreateStandardService;

import com.paravai.regulations.standards.application.command.update.ChangeStandardizationBodyService;
import com.paravai.regulations.standards.application.command.update.ChangeStandardTypeService;
import com.paravai.regulations.standards.application.command.update.UpdateStandardService;
import com.paravai.regulations.standards.application.command.version.AddStandardVersionService;
import com.paravai.regulations.standards.application.query.find.FindStandardByIdService;
import com.paravai.regulations.standards.application.query.search.SearchStandardsService;
import com.paravai.regulations.standards.domain.value.StandardTypeValue;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v1/standards")
@Tag(name = "Standards", description = "Operations related to Standards")
public class StandardController {

    private static final Logger log = LoggerFactory.getLogger(StandardController.class);

    private final CreateStandardService createService;
    private final UpdateStandardService updateService;
    private final AddStandardVersionService addVersionService;
    private final ChangeStandardizationBodyService changeIssuingBodyService;
    private final ChangeStandardTypeService changeStandardTypeService;
    private final AddApplicabilityContextService addApplicabilityContextService;
    private final RemoveApplicabilityContextService removeApplicabilityContextService;


    private final FindStandardByIdService findByIdService;
    private final SearchStandardsService searchService;


    private final MessageService messageService;

    public StandardController(CreateStandardService createService,
                              UpdateStandardService updateService,
                              AddStandardVersionService addVersionService,
                              ChangeStandardizationBodyService changeIssuingBodyService,
                              ChangeStandardTypeService changeStandardTypeService,
                              AddApplicabilityContextService addApplicabilityContextService,
                              RemoveApplicabilityContextService removeApplicabilityContextService,
                              FindStandardByIdService findByIdService,
                              SearchStandardsService searchStandardsService,
                              MessageService messageService) {
        this.createService = createService;
        this.updateService = updateService;
        this.addVersionService = addVersionService;
        this.changeIssuingBodyService = changeIssuingBodyService;
        this.changeStandardTypeService = changeStandardTypeService;
        this.addApplicabilityContextService = addApplicabilityContextService;
        this.removeApplicabilityContextService = removeApplicabilityContextService;
        this.findByIdService = findByIdService;
        this.searchService = searchStandardsService;
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Standard")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Standard created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> create(
            @Valid @RequestBody JsonApiRequest<CreateStandardRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final CreateStandardRequest dto = request.getData().getAttributes();

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] POST /v1/standards - creating standard", traceId, userOid);

            return createService.create(dto.toDomain())
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .path("/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Standard {} created", traceId, userOid, resp.getId());

                                return ResponseEntity
                                        .created(location)
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });
    }

    @PostMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add a version to an existing Standard")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Standard updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Standard not found", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> addVersion(
            @PathVariable("id") String id,
            @Valid @RequestBody JsonApiRequest<AddStandardVersionRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final AddStandardVersionRequest dto = request.getData().getAttributes();
        final IdValue standardId = IdValue.of(id);

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] POST /v1/standards/{}/versions - adding version", traceId, userOid, id);

            return addVersionService.addVersion(
                            standardId,
                            dto.toVersionVo(),
                            dto.toPublicationDateVoOrNull(),
                            dto.toVisibilityVo(),
                            dto.toStatusVo(),
                            dto.toVersionDescriptionOrNull()
                    )
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .replacePath("/v1/standards/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Added version to Standard {}", traceId, userOid, resp.getId());

                                return ResponseEntity
                                        .ok()
                                        .header("Location", location.toString())
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });

    }


    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update Standard metadata (versions are immutable)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Standard updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Standard not found", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> update(
            @PathVariable("id") String id,
            @Valid @RequestBody JsonApiRequest<UpdateStandardRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final UpdateStandardRequest dto = request.getData().getAttributes();
        final IdValue standardId = IdValue.of(id);

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] PUT /v1/standards/{} - updating standard metadata", traceId, userOid, id);

            return updateService.update(
                            standardId,
                            dto.toCodeVo(),
                            dto.toTitleVo(),
                            dto.toDescriptionOrNull()
                    )
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .replacePath("/v1/standards/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Standard {} updated", traceId, userOid, resp.getId());

                                return ResponseEntity
                                        .ok()
                                        .header("Location", location.toString())
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });

    }

    @PatchMapping("/{id}/standardization-body")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Change the issuing body of an existing Standard (versions are not affected)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Standard updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Standard not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Domain conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> ChangeStandardizationBody(
            @PathVariable("id") String id,
            @Valid @RequestBody JsonApiRequest<ChangeStandardizationBodyRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final ChangeStandardizationBodyRequest dto = request.getData().getAttributes();
        final IdValue standardId = IdValue.of(id);

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] PATCH /v1/standards/{}/issuing-body - changing issuing body", traceId, userOid, id);

            return changeIssuingBodyService.changeStandardizationBody(
                            standardId,
                            OrganizationAssociationValue.forStandardIssuer(IdValue.of(dto.getStandardizationBodyId()))
                    )
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .replacePath("/v1/standards/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Standard {} issuingBody changed", traceId, userOid, resp.getId());

                                return ResponseEntity
                                        .ok()
                                        .header("Location", location.toString())
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });

    }

    @PatchMapping("/{id}/type")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Change the type of an existing Standard (versions are not affected)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Standard updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Standard not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Domain conflict", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> changeType(
            @PathVariable("id") String id,
            @Valid @RequestBody JsonApiRequest<ChangeStandardTypeRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final ChangeStandardTypeRequest dto = request.getData().getAttributes();
        final IdValue standardId = IdValue.of(id);

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] PATCH /v1/standards/{}/type - changing type", traceId, userOid, id);

            return changeStandardTypeService.changeType(
                            standardId,
                            StandardTypeValue.of(dto.getStandardTypeCode())
                    )
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .replacePath("/v1/standards/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Standard {} type changed", traceId, userOid, resp.getId());

                                return ResponseEntity
                                        .ok()
                                        .header("Location", location.toString())
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });

    }


    @PostMapping("/{id}/versions/{versionId}/applicability-contexts")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add an Applicability Context to a Standard Version")
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> addApplicabilityContext(
            @PathVariable("id") String id,
            @PathVariable("versionId") String versionId,
            @Valid @RequestBody JsonApiRequest<AddApplicabilityContextRequest> request,
            ServerHttpRequest httpRequest
    ) {
        final AddApplicabilityContextRequest dto = request.getData().getAttributes();

        final IdValue standardId = IdValue.of(id);
        final IdValue vId = IdValue.of(versionId);

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] POST /v1/standards/{}/versions/{}/applicability-contexts - adding context",
                    traceId, userOid, id, versionId);

            return addApplicabilityContextService.addContext(
                            standardId,
                            vId,
                            dto.toCertificationSchemeId(),
                            dto.toEffectiveDateVo(),
                            dto.toEndOfValidityDateVoOrNull()
                    )
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .replacePath("/v1/standards/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Added applicability context to Standard {} version {}",
                                        traceId, userOid, resp.getId(), versionId);

                                return ResponseEntity
                                        .ok()
                                        .header("Location", location.toString())
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });
    }

    @DeleteMapping("/{id}/versions/{versionId}/applicability-contexts/{contextId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove an Applicability Context from a Standard Version")
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> removeApplicabilityContext(
            @PathVariable("id") String id,
            @PathVariable("versionId") String versionId,
            @PathVariable("contextId") String contextId,
            ServerHttpRequest httpRequest
    ) {
        final IdValue standardId = IdValue.of(id);
        final IdValue vId = IdValue.of(versionId);
        final IdValue cId = IdValue.of(contextId);

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] DELETE /v1/standards/{}/versions/{}/applicability-contexts/{} - removing context",
                    traceId, userOid, id, versionId, contextId);

            return removeApplicabilityContextService.removeContext(standardId, vId, cId)
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                URI location = UriComponentsBuilder.fromUri(httpRequest.getURI())
                                        .replacePath("/v1/standards/{id}")
                                        .build(resp.getId());

                                log.info("[{}][{}] Removed applicability context {} from Standard {} version {}",
                                        traceId, userOid, contextId, resp.getId(), versionId);

                                return ResponseEntity
                                        .ok()
                                        .header("Location", location.toString())
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    );
        });
    }


    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get a Standard by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Standard found"),
            @ApiResponse(responseCode = "404", description = "Standard not found", content = @Content)
    })
    public Mono<ResponseEntity<JsonApiSingleResponse<StandardResponse>>> findById(
            @PathVariable("id") String id,
            ServerHttpRequest httpRequest
    ) {
        final IdValue standardId = IdValue.of(id);

        return Mono.deferContextual(ctx -> {
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);
            final Locale locale = LocaleContext.getOrDefault(ctx);

            log.debug("[{}][{}] GET /v1/standards/{} - find by id", traceId, userOid, id);

            return findByIdService.findById(standardId)
                    .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                    .flatMap(resp ->
                            JsonApiResponseBuilder.buildSingle(
                                    Mono.just(resp),
                                    httpRequest,
                                    "standards",
                                    StandardResponse::getId
                            ).map(body -> {
                                log.info("[{}][{}] Standard {} returned", traceId, userOid, resp.getId());
                                return ResponseEntity
                                        .ok()
                                        .contentType(MediaType.valueOf("application/vnd.api+json"))
                                        .body(body);
                            })
                    )
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("[{}][{}] Standard {} not found", traceId, userOid, id);
                        return Mono.just(ResponseEntity.notFound().build());
                    }));
        });
    }

    // Paginated list
    @GetMapping
    @Operation(summary = "Get paginated list of Standards")
    public Mono<com.paravai.foundation.viewjsonapi.jsonapi.JsonApiResponse<StandardResponse>> getAll(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam Map<String, String> filters,
            ServerHttpRequest request
    ) {
        final PaginationRequest pagination = new PaginationRequest(page, size);
        final Map<String, String> domainFilters = sanitizeFilters(filters);

        final SearchQueryValue query = SearchQueryValue.of(
                domainFilters,
                Optional.ofNullable(search),
                Optional.ofNullable(sort),
                pagination
        );

        return Mono.deferContextual(ctx -> {
            final Locale locale = LocaleContext.getOrDefault(ctx);
            final String traceId = RequestContext.getTraceId(ctx);
            final String userOid = RequestContext.getUserOid(ctx);

            log.debug(
                    "[{}][{}] GET /v1/standards - filters={}, search={}, sort={}, page={}, size={}",
                    traceId, userOid,
                    domainFilters.keySet(), search, sort, pagination.getPage(), pagination.getSize()
            );

            if (pagination.getSize() > 200) {
                return Mono.error(new IllegalArgumentException("Page size exceeds maximum allowed (200)"));
            }

            return JsonApiResponseBuilder.buildPaginated(
                    searchService.search(query)
                            .map(s -> StandardResponse.fromDomain(s, locale, messageService))
                            .doOnNext(resp -> log.debug("[{}][{}] Fetched standard {}", traceId, userOid, resp.getId())),
                    searchService.count(query)
                            .doOnSuccess(count -> log.debug(
                                    "[{}][{}] Retrieved {} standards with filters {}",
                                    traceId, userOid, count, domainFilters.keySet()
                            )),
                    pagination,
                    request,
                    "standards",
                    StandardResponse::getId
            ).doOnError(ex -> log.error(
                    "[{}][{}] Failed to fetch paginated standards with query={}",
                    traceId, userOid, query, ex
            ));
        });
    }

    // Helpers
    private static Map<String, String> sanitizeFilters(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) return Map.of();
        Map<String, String> copy = new HashMap<>(raw);
        copy.remove("page");
        copy.remove("size");
        copy.remove("sort");
        copy.remove("search");
        return copy;
    }


}
