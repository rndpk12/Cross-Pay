package com.crosspay.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception, HttpServletRequest request
    ) {
        LOGGER.debug("event=resource_not_found path={}", request.getRequestURI());
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception, HttpServletRequest request
    ) {
        LOGGER.debug("event=business_request_rejected path={}", request.getRequestURI());
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request
    ) {
        LOGGER.debug("event=validation_failed path={}", request.getRequestURI());
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception, HttpServletRequest request
    ) {
        LOGGER.debug("event=validation_failed path={}", request.getRequestURI());
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage())
        );
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
            Exception exception, HttpServletRequest request
    ) {
        LOGGER.debug("event=malformed_request path={}", request.getRequestURI());
        return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Malformed request", request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception, HttpServletRequest request
    ) {
        LOGGER.warn("event=database_constraint_conflict path={}", request.getRequestURI());
        return response(HttpStatus.CONFLICT, "CONFLICT",
                "The request conflicts with existing data", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request
    ) {
        LOGGER.error("event=unexpected_error path={}", request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ApiErrorResponse> response(
            HttpStatus status, String error, String message,
            HttpServletRequest request, Map<String, String> fieldErrors
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now(), status.value(), error, message,
                request.getRequestURI(), fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
