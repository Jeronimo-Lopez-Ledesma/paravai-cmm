package com.paravai.foundation.viewjsonapi.jsonapi;

import com.paravai.foundation.domain.model.Identifiable;
import com.paravai.foundation.viewjsonapi.pagination.PaginationRequest;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class JsonApiResponseBuilder {
    /**
     * JSON:API response for a single item (Mono)
     */
    public static <T> Mono<JsonApiSingleResponse<T>> buildSingle(
            Mono<T> monoItem,
            ServerHttpRequest request,
            String resourceType,
            Function<T, String> idExtractor
    ) {


        return monoItem.map(item -> {

            String selfUrl = request.getURI().getPath() + "/" + idExtractor.apply(item);

            return JsonApiSingleResponse.<T>builder()
                    .data(JsonApiResource.<T>builder()
                            .id(idExtractor.apply(item))
                            .type(resourceType)
                            .attributes(item)
                            .build())
                    .link("self", selfUrl)
                    .build();
        });
    }

    public static <T extends Identifiable> JsonApiResponse<T> buildSingleSync(
            T item,
            ServerHttpRequest request,
            String resourceType,
            Function<T, String> idExtractor
    ) {
        String selfUrl = request.getURI().getPath() + "/" + idExtractor.apply(item);

        return JsonApiResponse.<T>builder()
                .data(List.of(JsonApiResource.<T>builder()
                        .id(idExtractor.apply(item))
                        .type(resourceType)
                        .attributes(item)
                        .build()))
                .link("self", selfUrl)
                .build();
    }


    /**
     * JSON:API response for a paginated collection (Flux)
     */
    public static <T> Mono<JsonApiResponse<T>> buildPaginated(Flux<T> dataFlux,
                                                              Mono<Long> totalCountMono,
                                                              PaginationRequest pagination,
                                                              ServerHttpRequest request,
                                                              String resourceType,
                                                              Function<T, String> idExtractor) {

        return dataFlux.collectList().zipWith(totalCountMono)
                .map(tuple -> {
                    List<T> items = tuple.getT1();
                    long total = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) total / pagination.getSize());

                    String baseUrl = request.getURI().getPath();
                    int currentPage = pagination.getPage();
                    int sizeParam = pagination.getSize();

                    Map<String, String> links = new HashMap<>();
                    links.put("self", baseUrl + "?page=" + currentPage + "&size=" + sizeParam);
                    if (currentPage < totalPages) {
                        links.put("next", baseUrl + "?page=" + (currentPage + 1) + "&size=" + sizeParam);
                    }
                    if (currentPage > 1) {
                        links.put("prev", baseUrl + "?page=" + (currentPage - 1) + "&size=" + sizeParam);
                    }

                    List<JsonApiResource<T>> resourceList = items.stream()
                            .map(item -> JsonApiResource.<T>builder()
                                    .id(idExtractor.apply(item))
                                    .type(resourceType)
                                    .attributes(item)
                                    .build())
                            .toList();

                    return JsonApiResponse.<T>builder()
                            .data(resourceList)
                            .meta(Map.of(
                                    "page", currentPage,
                                    "size", sizeParam,
                                    "totalElements", total,
                                    "totalPages", totalPages
                            ))
                            .links(links)
                            .build();
                });
    }

    public static <T> Mono<JsonApiResponse<T>> buildUnpaginated(
            Mono<List<T>> itemsMono,
            ServerHttpRequest request,
            String resourceType,
            Function<T, String> idExtractor
    ) {
        String selfUrl = request.getURI().getPath();

        return itemsMono.map(items -> JsonApiResponse.<T>builder()
                .data(
                        items.stream()
                                .map(item -> JsonApiResource.<T>builder()
                                        .id(idExtractor.apply(item))
                                        .type(resourceType)
                                        .attributes(item)
                                        .build()
                                ).toList()
                )
                .link("self", selfUrl)
                .build()
        );

    }

    /**
     * JSON:API composite response builder for orchestrators.
     * Supports pre-assembled JSON structures that already contain "data", "included", and "relationships".
     *
     * This method is intentionally separated from buildSingle() and buildSimpleSingle()
     * to avoid affecting domain services that return single aggregates.
     */
    public static <T> JsonApiSingleResponse<T> buildComposite(T content, String resourceType, String selfUrl) {

        return JsonApiSingleResponse.<T>builder()
                .data(JsonApiResource.<T>builder()
                        .id(null) // Composite responses usually have no unique ID
                        .type(resourceType)
                        .attributes(content)
                        .build())
                .link("self", selfUrl)
                .build();
    }


    /*
    private static <T> String extractId(T obj) {
        try {
            var method = obj.getClass().getMethod("getId");
            Object result = method.invoke(obj);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract 'id' from object", e);
        }
    }
*/


}
