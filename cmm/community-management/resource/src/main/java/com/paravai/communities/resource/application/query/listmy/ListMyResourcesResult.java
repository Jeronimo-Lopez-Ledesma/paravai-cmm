package com.paravai.communities.resource.application.query.listmy;

import com.paravai.communities.resource.domain.model.Resource;

import java.util.List;
import java.util.Objects;

public final class ListMyResourcesResult {

    private final List<Resource> items;
    private final long total;
    private final int page;
    private final int size;

    private ListMyResourcesResult(
            List<Resource> items,
            long total,
            int page,
            int size
    ) {
        this.items = List.copyOf(Objects.requireNonNull(items, "items is required"));
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public static ListMyResourcesResult of(
            List<Resource> items,
            long total,
            int page,
            int size
    ) {
        return new ListMyResourcesResult(items, total, page, size);
    }

    public List<Resource> items() {
        return items;
    }

    public long total() {
        return total;
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }
}