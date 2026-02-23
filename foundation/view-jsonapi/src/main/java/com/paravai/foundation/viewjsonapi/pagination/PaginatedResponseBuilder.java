package com.paravai.foundation.viewjsonapi.pagination;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaginatedResponseBuilder {

    public static <T> Mono<PaginatedResponse<T>> build(
            Flux<T> dataFlux,
            Mono<Long> totalCountMono,
            PaginationRequest pagination,
            ServerHttpRequest request
    ) {
        return dataFlux.collectList().zipWith(totalCountMono)
                .map(tuple -> {
                    List<T> items = tuple.getT1();
                    long total = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) total / pagination.getSize());

                    String baseUrl = request.getURI().getPath();
                    Map<String, String> links = new HashMap<>();
                    links.put("self", baseUrl + "?page=" + pagination.getPage() + "&size=" + pagination.getSize());
                    if (pagination.getPage() < totalPages) {
                        links.put("next", baseUrl + "?page=" + (pagination.getPage() + 1) + "&size=" + pagination.getSize());
                    }
                    if (pagination.getPage() > 1) {
                        links.put("prev", baseUrl + "?page=" + (pagination.getPage() - 1) + "&size=" + pagination.getSize());
                    }

                    return PaginatedResponse.<T>builder()
                            .data(items)
                            .meta(new PaginatedResponse.Meta(
                                    pagination.getPage(),
                                    pagination.getSize(),
                                    total,
                                    totalPages
                            ))
                            .links(links)
                            .build();
                });
    }
}
