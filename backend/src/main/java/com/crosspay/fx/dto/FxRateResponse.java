package com.crosspay.fx.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record FxRateResponse(
        UUID id,
        String baseCurrency,
        String quoteCurrency,
        BigDecimal rate,
        String source,
        OffsetDateTime fetchedAt,
        OffsetDateTime createdAt
) {
}