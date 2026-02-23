package com.dekra.service.regulations.standards.api.rest.v1;

import com.dekra.service.foundation.localization.LocaleContext;
import com.dekra.service.foundation.localization.MessageService;
import com.dekra.service.regulations.standards.api.rest.v1.dto.StandardTypeResponse;
import com.dekra.service.regulations.standards.api.rest.v1.dto.StandardVersionStatusResponse;
import com.dekra.service.regulations.standards.application.catalog.StandardCatalogService;
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
@RequestMapping("/v1/standards/catalog")
@Tag(name = "Catalogs", description = "Reference data for standards service")
public class StandardCatalogController {

    private final StandardCatalogService catalogService;
    private final MessageService messageService;

    public StandardCatalogController(StandardCatalogService catalogService, MessageService messageService) {
        this.catalogService = catalogService;
        this.messageService = messageService;
    }

    @GetMapping("/standard-types")
    @Operation(
            summary = "Get standard types catalog",
            description = "Returns the list of available standard type values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catalog retrieved successfully")
    })
    public Flux<StandardTypeResponse> getStandardTypesCatalog() {
        return Mono.deferContextual(ctx -> {
            Locale locale = LocaleContext.getOrDefault(ctx);

            return catalogService.getStandardTypeCatalog()
                    .map(v -> StandardTypeResponse.fromDomain(v, locale, messageService))
                    .collectList();
        }).flatMapMany(Flux::fromIterable);
    }

    @GetMapping("/standard-version-statuses")
    @Operation(
            summary = "Get standard version statuses catalog",
            description = "Returns the list of available standard version status values"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catalog retrieved successfully")
    })
    public Flux<StandardVersionStatusResponse> getStandardVersionStatusesCatalog() {
        return Mono.deferContextual(ctx -> {
            Locale locale = LocaleContext.getOrDefault(ctx);

            return catalogService.getStandardVersionStatusCatalog()
                    .map(v -> StandardVersionStatusResponse.fromDomain(v, locale, messageService))
                    .collectList();
        }).flatMapMany(Flux::fromIterable);
    }

}