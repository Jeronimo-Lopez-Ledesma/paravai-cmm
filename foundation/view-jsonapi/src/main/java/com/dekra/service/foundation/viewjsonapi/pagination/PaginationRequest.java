package com.dekra.service.foundation.viewjsonapi.pagination;

public class PaginationRequest {

    private final int page;
    private final int size;

    public PaginationRequest(Integer page, Integer size) {
        this.page = (page != null && page >= 1) ? page : PaginationConstants.DEFAULT_PAGE;
        this.size = (size != null && size >= 1) ? Math.min(size, PaginationConstants.MAX_SIZE) : PaginationConstants.DEFAULT_SIZE;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getZeroBasedPage() {
        return page - 1;
    }
}
