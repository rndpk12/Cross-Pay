package com.crosspay.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsIllegalArgumentToSafeBadRequest() {
        var response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Insufficient balance"), request("/api/v1/withdrawals")
        );
        assertEquals(400, response.getStatusCode().value());
        assertEquals("BAD_REQUEST", response.getBody().error());
        assertEquals("/api/v1/withdrawals", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void mapsNotFoundTo404() {
        var response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Wallet not found for this currency"), request("/api/v1/wallets/USD")
        );
        assertEquals(404, response.getStatusCode().value());
        assertEquals("NOT_FOUND", response.getBody().error());
    }

    @Test
    void hidesDatabaseDetailsInConflict() {
        var response = handler.handleDataIntegrityViolationException(
                new DataIntegrityViolationException("duplicate key violates uq_user_currency"), request("/api/v1/wallets")
        );
        assertEquals(409, response.getStatusCode().value());
        assertEquals("The request conflicts with existing data", response.getBody().message());
        assertNull(response.getBody().fieldErrors());
    }

    @Test
    void hidesUnexpectedExceptionMessage() {
        var response = handler.handleUnexpectedException(
                new RuntimeException("sensitive database failure"), request("/api/v1/transfers")
        );
        assertEquals(500, response.getStatusCode().value());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    private HttpServletRequest request(String path) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(path);
        return request;
    }
}
