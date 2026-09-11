package com.crosspay.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}