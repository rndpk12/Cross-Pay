package com.crosspay.auth.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String country,
        String status
) {}