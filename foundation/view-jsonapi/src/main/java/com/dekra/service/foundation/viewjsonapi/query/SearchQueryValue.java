package com.dekra.service.foundation.viewjsonapi.query;

import com.dekra.service.foundation.viewjsonapi.pagination.PaginationRequest;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record SearchQueryValue(
        FilterSetValue filters,
        SearchTextValue search,
        SortSpecValue sort,
        PaginationRequest page
) {
    public SearchQueryValue {
        filters = (filters == null) ? FilterSetValue.empty() : filters;
        search  = (search  == null) ? SearchTextValue.EMPTY   : search;
        sort    = (sort    == null) ? SortSpecValue.empty()   : sort;
        Objects.requireNonNull(page, "page must not be null");
    }

    public static SearchQueryValue of(Map<String,String> rawFilters,
                                      Optional<String> rawSearch,
                                      Optional<String> rawSort,
                                      PaginationRequest page) {
        return new SearchQueryValue(
                FilterSetValue.of(rawFilters),
                SearchTextValue.of(rawSearch.orElse(null)),
                SortSpecValue.parse(rawSort.orElse(null)),
                page
        );
    }
}
