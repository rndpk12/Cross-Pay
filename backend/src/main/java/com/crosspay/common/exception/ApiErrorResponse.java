package com.crosspay.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Standardized safe API error response")
public record ApiErrorResponse(
        @Schema(example = "2026-01-15T10:30:00Z")
        OffsetDateTime timestamp,
        @Schema(example = "400")
        int status,
        @Schema(example = "Bad Request")
        String error,
        @Schema(example = "Validation failed")
        String message,
        @Schema(example = "/api/v1/deposits")
        String path,
        @Schema(description = "Present for validation failures", example = "{\"amount\":\"Amount must be greater than zero\"}")
        Map<String, String> fieldErrors
) {
}
