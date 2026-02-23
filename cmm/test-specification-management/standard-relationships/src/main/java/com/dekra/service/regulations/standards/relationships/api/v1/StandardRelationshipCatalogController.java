package com.dekra.service.regulations.standards.relationships.api.v1;

import com.dekra.service.foundation.localization.LocaleContext;
import com.dekra.service.foundation.localization.MessageService;
import com.dekra.service.regulations.standards.relationships.api.v1.dto.StandardRelationshipPurposeResponse;
import com.dekra.service.regulations.standards.relationships.api.v1.dto.StandardRelationshipTypeResponse;
import com.dekra.service.regulations.standards.relationships.application.catalog.StandardRelationshipCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;

@RestController
@RequestMapping("/v1/standards/relationships/catalog")
@Tag(name = "Catalogs", description = "Reference data for standard relationships service")
public class StandardRelationshipCatalogController {

    private final StandardRelationshipCatalogService catalogService;
    private final MessageService messageService;

    public StandardRelationshipCatalogController(StandardRelationshipCatalogService catalogService,
                                                 MessageService messageService) {
        this.catalogService = catalogService;
        this.messageService = messageService;
    }

    @GetMapping("/relationship-types")
    @Operation(
            summary = "Get standard relationship types catalog",
            description = "Returns the list of available relationship type values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catalog retrieved successfully")
    })
    public Flux<StandardRelationshipTypeResponse> getRelationshipTypesCatalog() {
        return Mono.deferContextual(ctx -> {
            Locale locale = LocaleContext.getOrDefault(ctx);

            return catalogService.getRelationshipTypeCatalog()
                    .map(v -> StandardRelationshipTypeResponse.fromDomain(v, locale, messageService))
                    .collectList();
        }).flatMapMany(Flux::fromIterable);
    }

    @GetMapping("/relationship-purposes")
    @Operation(
            summary = "Get standard relationship purposes catalog",
            description = "Returns the list of available relationship purpose values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catalog retrieved successfully")
    })
    public Flux<StandardRelationshipPurposeResponse> getRelationshipPurposesCatalog() {
        return Mono.deferContextual(ctx -> {
            Locale locale = LocaleContext.getOrDefault(ctx);

            return catalogService.getRelationshipPurposeCatalog()
                    .map(v -> StandardRelationshipPurposeResponse.fromDomain(v, locale, messageService))
                    .collectList();
        }).flatMapMany(Flux::fromIterable);
    }
}
