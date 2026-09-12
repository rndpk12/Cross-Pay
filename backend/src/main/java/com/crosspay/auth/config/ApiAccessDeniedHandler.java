package com.crosspay.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAccessDeniedHandler.class);

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception
    ) throws IOException {
        LOGGER.warn("event=access_denied path={}", request.getRequestURI());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"timestamp\":\"" + OffsetDateTime.now() + "\",\"status\":403,\"error\":\"FORBIDDEN\","
                + "\"message\":\"Access is denied\",\"path\":\""
                + request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"")
                + "\"}");
    }
}
