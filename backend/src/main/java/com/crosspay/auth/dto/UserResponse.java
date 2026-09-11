package com.crosspay.user.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String country,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}