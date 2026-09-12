package com.crosspay.wallet.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String currency,
        BigDecimal balance,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}