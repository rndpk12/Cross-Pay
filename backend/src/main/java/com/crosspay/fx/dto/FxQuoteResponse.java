package com.crosspay.fx.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FxQuoteResponse(
        UUID id,
        String fromCurrency,
        String toCurrency,
        BigDecimal sourceAmount,
        BigDecimal exchangeRate,
        BigDecimal convertedAmount,
        BigDecimal feeAmount,
        String status,
        OffsetDateTime expiresAt,
        OffsetDateTime createdAt
) {
}