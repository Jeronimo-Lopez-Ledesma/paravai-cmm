package com.dekra.service.foundation.viewjsonapi.pagination;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationRequestTest {

    @Test
    void shouldUseDefaultValuesWhenNullProvided() {
        PaginationRequest request = new PaginationRequest(null, null);

        assertEquals(PaginationConstants.DEFAULT_PAGE, request.getPage());
        assertEquals(PaginationConstants.DEFAULT_SIZE, request.getSize());
        assertEquals(PaginationConstants.DEFAULT_PAGE - 1, request.getZeroBasedPage());
    }

    @Test
    void shouldClampSizeToMaxWhenExceedsLimit() {
        PaginationRequest request = new PaginationRequest(2, PaginationConstants.MAX_SIZE + 50);

        assertEquals(2, request.getPage());
        assertEquals(PaginationConstants.MAX_SIZE, request.getSize());
    }

    @Test
    void shouldFallbackToDefaultOnInvalidValues() {
        PaginationRequest request = new PaginationRequest(-3, -10);

        assertEquals(PaginationConstants.DEFAULT_PAGE, request.getPage());
        assertEquals(PaginationConstants.DEFAULT_SIZE, request.getSize());
    }

    @Test
    void shouldRespectValidPageAndSize() {
        PaginationRequest request = new PaginationRequest(3, 20);

        assertEquals(3, request.getPage());
        assertEquals(20, request.getSize());
        assertEquals(2, request.getZeroBasedPage());
    }
}
