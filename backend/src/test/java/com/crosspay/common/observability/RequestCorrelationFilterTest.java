package com.crosspay.common.observability;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Test
    void preservesSafeRequestIdAndClearsMdcAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/wallets");
        request.addHeader(RequestCorrelationFilter.HEADER_NAME, "build-42.request_7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
                assertEquals("build-42.request_7", MDC.get(RequestCorrelationFilter.MDC_KEY)));

        assertEquals("build-42.request_7", response.getHeader(RequestCorrelationFilter.HEADER_NAME));
        assertEquals(null, MDC.get(RequestCorrelationFilter.MDC_KEY));
    }

    @Test
    void replacesUnsafeOrOversizedRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.addHeader(RequestCorrelationFilter.HEADER_NAME, "unsafe\r\nheader: injected" + "x".repeat(80));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> { });

        String generated = response.getHeader(RequestCorrelationFilter.HEADER_NAME);
        assertNotNull(generated);
        assertFalse(generated.contains("\r"));
        assertFalse(generated.contains("\n"));
        assertEquals(36, generated.length());
    }
}
