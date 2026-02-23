package com.dekra.service.foundation.viewjsonapi.pagination;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PaginatedResponse<T> {

    private final List<T> data;
    private final Meta meta;
    private final Map<String, String> links;

    @Getter
    @Builder
    public static class Meta {
        private final int page;
        private final int size;
        private final long totalElements;
        private final int totalPages;
    }
}
