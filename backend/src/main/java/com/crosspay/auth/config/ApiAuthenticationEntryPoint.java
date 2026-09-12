package com.crosspay.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAuthenticationEntryPoint.class);

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        LOGGER.warn("event=authentication_required path={}", request.getRequestURI());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"timestamp\":\"" + OffsetDateTime.now() + "\",\"status\":401,\"error\":\"UNAUTHORIZED\","
                + "\"message\":\"Authentication is required\",\"path\":\""
                + request.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"")
                + "\"}");
    }
}
